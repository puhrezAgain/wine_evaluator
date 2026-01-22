package com.wine_evaluator.wine_evaluator.winequery

import org.springframework.stereotype.Service
import com.wine_evaluator.wine_evaluator.persistence.DocumentPriceSignalRepository

@Service
class WineQueryService(
    private val repository: DocumentPriceSignalRepository
) {
    fun query(
        queryTokens: Set<String>,
        queryPrice: Int,
        minScore: Double = 0.55
    ): List<WineMatch> {
        if (queryTokens.isEmpty()) return emptyList()

        val candidates = repository.findCandidatesByTokens(queryTokens)

        return candidates
            .mapNotNull { signal ->
                val score = hybridScore(queryTokens, signal.identityTokens)
                println("$queryTokens ${signal.identityTokens} $score")
                if (score < minScore) return@mapNotNull null

                // use max assuming bottle price
                val refPrice = signal.priceHints.maxOrNull() ?: return@mapNotNull null

                if (refPrice == 0) return@mapNotNull null

                val matchTokens = queryTokens.intersect(signal.identityTokens)
                val delta = queryPrice - refPrice
                val deltaPercent = (delta.toDouble() / refPrice) * 100

                WineMatch(
                    signalId = signal.id!!,
                    jaccard = score,
                    referencePrice = refPrice,
                    delta = delta,
                    deltaPercent = deltaPercent,
                    matchTokens = matchTokens
                )
            }
            .sortedByDescending { it.jaccard }
    }
}