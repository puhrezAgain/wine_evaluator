package com.wineevaluator.wine

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.interpretation.toIdentitySet
import com.wineevaluator.document.interpretation.tokenize
import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.document.persistence.JpaPriceSignalRepository
import com.wineevaluator.wine.matching.hybridScore
import com.wineevaluator.wine.model.WineMatch
import org.springframework.stereotype.Service

private const val MIN_MATCH_SCORE = 0.55

@Service
class WineQueryHandler(
    private val repository: JpaPriceSignalRepository,
) {
    fun query(
        query: String,
        queryPrice: Int,
    ): List<WineMatch> {
        val queryTokens =
            query
                .let(::tokenize)
                .let(::toIdentitySet)

        if (queryTokens.isEmpty()) return emptyList()

        val candidates = repository.findCandidatesByTokens(queryTokens)

        return candidates
            .mapNotNull { calculateMatch(it, queryTokens, queryPrice) }
            .sortedByDescending { it.jaccard }
    }

    fun queryByUploadId(id: UploadId): List<WineMatch> {
        val signals = repository.findByUploadId(id)

        if (signals.isEmpty()) return emptyList()

        val allTokens =
            signals
                .flatMap { it.tokens }
                .toSet()

        val candidates = repository.findCandidatesByTokens(allTokens)

        if (candidates.isEmpty()) return emptyList()

        // Instead of N+1 DB queries (one per signal),
        // fetch the superset of candidates once and score locally.
        // Same semantics, fewer round trips.
        return signals.mapNotNull { signal ->
            val price = signal.prices.maxOrNull() ?: return@mapNotNull null

            candidates
                .mapNotNull { candidate ->
                    calculateMatch(candidate, signal.tokens, price)
                }.maxByOrNull { it.jaccard }
        }
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
        val deltaPercent = (delta.toDouble() / refPrice) * 100

        return WineMatch(
            signalId = signal.uploadId.value,
            tokens = tokens,
            price = price,
            jaccard = score,
            referencePrice = refPrice,
            delta = delta,
            deltaPercent = deltaPercent,
            matchTokens = matchTokens,
        )
    }
}
