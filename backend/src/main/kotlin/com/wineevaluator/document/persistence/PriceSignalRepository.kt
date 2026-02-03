package com.wineevaluator.document.persistence

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.BottlePricedSignal
import com.wineevaluator.document.model.PriceSignal

interface PriceSignalRepository {
    fun write(signals: List<PriceSignal>)

    fun findCandidatesByTokens(tokens: Set<String>): List<BottlePricedSignal>

    fun findByUploadId(id: UploadId): List<BottlePricedSignal>
}
