package com.wineevaluator.document.model

import com.wineevaluator.common.value.UploadId

data class PriceSignal(
        val uploadId: UploadId,
        val tokens: Set<String>,
        val prices: List<Int>,
        val rawLine: String,
) {
        fun toBottlePricedSignal(): BottlePricedSignal? {
                val bottlePrice = prices.maxOrNull() ?: return null
                return BottlePricedSignal(uploadId, tokens, bottlePrice, rawLine)
        }
}

data class BottlePricedSignal(
        val uploadId: UploadId,
        val tokens: Set<String>,
        val price: Int,
        val rawLine: String,
)
