package com.wineevaluator.wine

import org.springframework.stereotype.Service
import com.wineevaluator.document.persistence.JpaPriceSignalRepository
import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.interpretation.tokenize
import com.wineevaluator.document.interpretation.toIdentitySet
import com.wineevaluator.wine.model.WineMatch
import com.wineevaluator.wine.matching.hybridScore

@Service
class WineQueryHandler(
    private val repository: JpaPriceSignalRepository
) {
    fun query(
        query: String,
        queryPrice: Int,
    ): List<WineMatch> {
        val queryTokens = query
            .let(::tokenize)
            .let(::toIdentitySet)

        if (queryTokens.isEmpty()) return emptyList()

        val candidates = repository.findCandidatesByTokens(queryTokens)

        return candidates
            .mapNotNull { calculateMatch(it, queryTokens, queryPrice)}
            .sortedByDescending { it.jaccard }
    }

    fun queryUpload(id: UploadId): List<WineMatch> {
        return repository.findByUploadId(id)
            .map{ signal ->
                repository.findCandidatesByTokens(signal.tokens)
                    .mapNotNull{ candidate ->
                        calculateMatch(candidate, signal.tokens, signal.prices.max())
                    }
                    .sortedByDescending { it.jaccard }
                    .first()
            }
    }

    private fun calculateMatch(signal: PriceSignal, tokens: Set<String>, price: Int, minScore: Double = 0.55): WineMatch? {
        val score = hybridScore(tokens, signal.tokens)

        if (score < minScore) return null

        // use max assuming bottle price
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