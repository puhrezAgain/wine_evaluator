package com.wine_evaluator.wine_evaluator.interpretation

data class TokenizedRow (
    // We match on token overlap since substring gymnastics is futile due to wine list variability
    val identityTokens: List<String>,
    // We don't worry about cents since our integration is aggregation
    val priceHintTokens: List<String>,
    // We keep the raw row for posterity
    val rawRow: String
)