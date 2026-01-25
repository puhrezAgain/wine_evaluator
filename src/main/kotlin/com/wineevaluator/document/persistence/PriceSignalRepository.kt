package com.wineevaluator.document.persistence

import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.common.value.UploadId

interface PriceSignalRepository {
    fun write(signals: List<PriceSignal>)
    fun findCandidatesByTokens(tokens: Set<String>): List<PriceSignal>
    fun findByUploadId(id: UploadId): List<PriceSignal>
}