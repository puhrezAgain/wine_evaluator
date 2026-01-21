package com.wine_evaluator.wine_evaluator.interpretation

data class ProcessedRow (
    // We match on token overlap since substring gymnastics is futile due to wine list variability
    val identityTokens: Set<String>,
    // We don't worry about cents since our integration is aggregation
    val priceTokens: List<Int>,
    // We keep the raw row for posterity
    val rawRow: String
)