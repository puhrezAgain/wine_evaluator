package com.wine_evaluator.wine_evaluator.ocr

data class OcrLine(
    val text: String,
    val centerX: Float,
    val baselineY: Float,
    val confidence: Float
)