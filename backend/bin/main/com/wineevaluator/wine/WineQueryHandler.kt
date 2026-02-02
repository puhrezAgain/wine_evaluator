package com.wineevaluator.wine

import com.wineevaluator.common.error.ValidationException
import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.interpretation.toIdentityTokens
import com.wineevaluator.document.interpretation.tokenize
import com.wineevaluator.document.model.BottlePricedSignal
import com.wineevaluator.document.persistence.PriceSignalRepository
import com.wineevaluator.wine.matching.MatchStrategy
import com.wineevaluator.wine.matching.hybridScore
import com.wineevaluator.wine.model.NewWine
import com.wineevaluator.wine.model.WineMatch
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.wine.model.WineResult
import kotlin.math.roundToInt
import org.springframework.stereotype.Service

private const val MIN_MATCH_SCORE = 0.55

@Service
class WineQueryHandler(
        private val repository: PriceSignalRepository,
) {
        fun query(
                query: WineQueryRequest,
        ): List<WineMatch> {
                validateQuery(query)

                val queryTokens = query.wine.let(::tokenize).let(::toIdentityTokens)

                if (queryTokens.isEmpty()) return emptyList()

                val candidates =
                        repository.findCandidatesByTokens(queryTokens).let(::dedupIdenticalSignals)

                return candidates
                        .mapNotNull { calculateMatch(it, queryTokens, query.roundedPrice()) }
                        .sortedByDescending { it.jaccard }
        }

        fun queryByUploadId(
                id: UploadId,
                matchStrategy: MatchStrategy = MatchStrategy.AVERAGE
        ): List<WineResult> {
                val signals = repository.findByUploadId(id)

                if (signals.isEmpty()) return emptyList()

                val allTokens = signals.flatMap { it.tokens }.toSet()

                // Instead of getting candidates per signal (N+1 DB queries (one per signal)),
                // fetch the superset of candidates once and score locally.
                // Same semantics, fewer round trips.
                val candidates = repository.findCandidatesByTokens(allTokens)

                if (candidates.isEmpty()) return emptyList()

                return signals.mapNotNull {
                        applyMatchStrategyForSignal(it, candidates, matchStrategy)
                }
        }

        private fun dedupIdenticalSignals(signals: List<BottlePricedSignal>) =
                signals.distinctBy { it.tokens to it.price }

        private fun validateQuery(query: WineQueryRequest) {
                if (query.wine.isEmpty()) {
                        throw ValidationException("Wine name must not be empty")
                }

                if (query.price <= 0) {
                        throw ValidationException("Price must be positive")
                }
        }

        private fun applyMatchStrategyForSignal(
                signal: BottlePricedSignal,
                candidates: List<BottlePricedSignal>,
                matchStrategy: MatchStrategy
        ): WineResult? {
                val goodMatches =
                        candidates.mapNotNull {
                                // exclude self matches
                                if (it.uploadId == signal.uploadId) return@mapNotNull null
                                calculateMatch(it, signal.tokens, signal.price)
                        }

                if (goodMatches.isEmpty())
                        return NewWine(
                                queryUploadId = signal.uploadId,
                                price = signal.price,
                                tokens = signal.tokens
                        )

                return when (matchStrategy) {
                        MatchStrategy.BEST -> bestMatch(goodMatches)
                        MatchStrategy.AVERAGE -> aggregateMatches(signal, goodMatches)
                }
        }

        private fun bestMatch(goodMatches: List<WineMatch>): WineMatch =
                goodMatches.maxBy { it.jaccard }

        private fun aggregateMatches(
                signal: BottlePricedSignal,
                goodMatches: List<WineMatch>
        ): WineMatch {
                val refPrice = goodMatches.map { it.referencePrice }.average()
                val avgJaccard = goodMatches.map { it.jaccard }.average()
                val matchTokens = goodMatches.flatMap { it.matchTokens }.toSet()

                return WineMatch(
                        queryUploadId = signal.uploadId,
                        price = signal.price,
                        tokens = signal.tokens,
                        jaccard = avgJaccard,
                        referencePrice = refPrice.roundToInt(),
                        delta = signal.price - refPrice.roundToInt(),
                        deltaPercent = percentDelta(signal.price.toDouble(), refPrice),
                        matchTokens = matchTokens,
                )
        }

        private fun calculateMatch(
                signal: BottlePricedSignal,
                tokens: Set<String>,
                price: Int,
                minScore: Double = MIN_MATCH_SCORE,
        ): WineMatch? {
                val score = hybridScore(tokens, signal.tokens)

                if (score < minScore) return null

                val delta = price - signal.price
                val deltaPercent = percentDelta(price.toDouble(), signal.price.toDouble())

                return WineMatch(
                        queryUploadId = signal.uploadId,
                        tokens = tokens,
                        price = price,
                        jaccard = score,
                        referencePrice = signal.price,
                        delta = delta,
                        deltaPercent = deltaPercent,
                        matchTokens = signal.tokens,
                )
        }

        private fun percentDelta(price: Double, refPrice: Double) =
                ((price - refPrice) / refPrice) * 100
}
