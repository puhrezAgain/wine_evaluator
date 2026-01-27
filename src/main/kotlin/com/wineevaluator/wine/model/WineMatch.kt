package com.wineevaluator.wine.model

import java.util.UUID
import com.wineevaluator.common.value.UploadId

data class WineMatch(
    val queryUploadId: UploadId,
    val jaccard: Double,
    val price: Int,
    val referencePrice: Int,
    val delta: Int,
    val deltaPercent: Double,
    val matchTokens: Set<String>,
    val tokens: Set<String>,
)
