package com.wineevaluator.wine.model

import com.wineevaluator.common.value.UploadId

enum class WineResultType {
        MATCH,
        NEW_WINE
}

sealed interface WineResult {
        val queryUploadId: UploadId
        val tokens: Set<String>
        val price: Int
        val type: WineResultType
}

data class WineMatch(
        override val queryUploadId: UploadId,
        override val tokens: Set<String>,
        override val price: Int,
        val jaccard: Double,
        val referencePrice: Int,
        val delta: Int,
        val deltaPercent: Double,
        val matchTokens: Set<String>,
) : WineResult {
        override val type = WineResultType.MATCH
}

data class NewWine(
        override val queryUploadId: UploadId,
        override val tokens: Set<String>,
        override val price: Int,
) : WineResult {
        override val type = WineResultType.NEW_WINE
}
