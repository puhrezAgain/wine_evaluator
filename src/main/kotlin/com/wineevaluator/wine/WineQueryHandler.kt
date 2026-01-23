package com.wineevaluator.wine

import org.springframework.stereotype.Service
import com.wineevaluator.document.persistence.JpaPriceSignalRepository
import com.wineevaluator.wine.model.WineMatch
import com.wineevaluator.wine.matching.hybridScore

@Service
class WineQueryHandler(
    private val repository: JpaPriceSignalRepository
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