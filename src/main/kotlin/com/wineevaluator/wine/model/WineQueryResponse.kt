package com.wineevaluator.wine.model

data class WineQueryResponse(
    val original: String,
    val queryPrice: Float,
    val matches: List<WineMatch>,
)
