package com.wineevaluator.document.model

import com.wineevaluator.common.value.UploadId

data class PriceSignal(
    val uploadId: UploadId,
    val tokens: Set<String>,
    val prices: List<Int>,
    val rawLine: String
)