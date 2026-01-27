package com.wineevaluator.wine.model

import kotlin.math.roundToInt

data class WineQueryRequest(
        val wine: String,
        val price: Float,
) {
    fun roundedPrice(): Int = price.roundToInt()
}
