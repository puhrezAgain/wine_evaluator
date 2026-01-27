package com.wineevaluator.wine

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.interpretation.toIdentityTokens
import com.wineevaluator.document.interpretation.tokenize
import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.document.persistence.JpaPriceSignalRepository
import com.wineevaluator.wine.matching.hybridScore
import com.wineevaluator.wine.model.WineMatch
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.common.error.ValidationException

import org.springframework.stereotype.Service
import kotlin.math.roundToInt

private const val MIN_MATCH_SCORE = 0.55

@Service
class WineQueryHandler(
    private val repository: JpaPriceSignalRepository,
) {
    fun query(
        query: WineQueryRequest,
    ): List<WineMatch> {
        validateQuery(query)

        val queryTokens =
            query.wine
                .let(::tokenize)
                .let(::toIdentityTokens)

        if (queryTokens.isEmpty()) return emptyList()

        val candidates = repository.findCandidatesByTokens(queryTokens)

        return candidates
            .mapNotNull { calculateMatch(it, queryTokens, query.roundedPrice()) }
            .sortedByDescending { it.jaccard }
    }

    fun queryByUploadId(id: UploadId): List<WineMatch> {
        val signals = repository.findByUploadId(id)

        if (signals.isEmpty()) return emptyList()

        val allTokens =
            signals
                .flatMap { it.tokens }
                .toSet()

        // Instead of getting candidates per signal (N+1 DB queries (one per signal)),
        // fetch the superset of candidates once and score locally.
        // Same semantics, fewer round trips.
        val candidates = repository.findCandidatesByTokens(allTokens)

        if (candidates.isEmpty()) return emptyList()


        return signals.mapNotNull { aggregateMatchesForSignal(it, candidates) }
    }


    private fun validateQuery(query: WineQueryRequest) {
        if (query.wine.isEmpty()) {
            throw ValidationException("Wine name must not be empty")
        }

        if (query.price <= 0) {
            throw ValidationException("Price must be positive")
        }
    }
    private fun aggregateMatchesForSignal(signal: PriceSignal, candidates: List<PriceSignal>): WineMatch? {
        val price = signal.prices.maxOrNull() ?: return null

        val goodMatches = candidates
            .mapNotNull {
                // exclude self matches
                if (it.uploadId == signal.uploadId) return@mapNotNull null
                calculateMatch(it, signal.tokens, price)
            }

        if (goodMatches.isEmpty()) return null

        val refPrice = goodMatches.map { it.referencePrice }.average()
        val avgJaccard = goodMatches.map { it.jaccard }.average()
        val matchTokens = goodMatches.flatMap { it.matchTokens }.toSet()

        return WineMatch(
            queryUploadId = signal.uploadId,
            jaccard = avgJaccard,
            price = price,
            referencePrice = refPrice.roundToInt(),
            delta = price - refPrice.roundToInt(),
            deltaPercent = percentDelta(price.toDouble(), refPrice),
            matchTokens = matchTokens,
            tokens = signal.tokens
        )
    }

    private fun calculateMatch(
        signal: PriceSignal,
        tokens: Set<String>,
        price: Int,
        minScore: Double = MIN_MATCH_SCORE,
    ): WineMatch? {
        val score = hybridScore(tokens, signal.tokens)

        if (score < minScore) return null

        // assuming max leads us to bottle price
        val refPrice = signal.prices.maxOrNull() ?: return null

        if (refPrice == 0) return null

        val matchTokens = tokens.intersect(signal.tokens)
        val delta = price - refPrice
        val deltaPercent = percentDelta(price.toDouble(), refPrice.toDouble())

        return WineMatch(
            queryUploadId = signal.uploadId,
            tokens = tokens,
            price = price,
            jaccard = score,
            referencePrice = refPrice,
            delta = delta,
            deltaPercent = deltaPercent,
            matchTokens = matchTokens,
        )
    }

    private fun percentDelta(price: Double, refPrice: Double) =
        ((price - refPrice) / refPrice) * 100
}
