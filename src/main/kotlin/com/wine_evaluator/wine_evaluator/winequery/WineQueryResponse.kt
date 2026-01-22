package com.wine_evaluator.wine_evaluator.winequery

data class WineQueryResponse(
    val original: String,
    val queryPrice: Float,
    val matches: List<WineMatch>
)
