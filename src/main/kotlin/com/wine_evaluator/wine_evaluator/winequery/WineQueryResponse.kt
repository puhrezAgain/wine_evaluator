package com.wine_evaluator.wine_evaluator.winequery

data class WineQueryResponse(
    val original: String,
    val price: Float,
    val tokens: Set<String>
)
