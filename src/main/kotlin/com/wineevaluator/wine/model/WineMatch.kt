package com.wineevaluator.wine.model

import java.util.UUID

data class WineMatch(
    val signalId: UUID,
    val jaccard: Double,
    val referencePrice: Int,
    val delta: Int,
    val deltaPercent: Double,
    val matchTokens: Set<String>
)