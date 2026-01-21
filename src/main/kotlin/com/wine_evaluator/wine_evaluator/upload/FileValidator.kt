package com.wine_evaluator.wine_evaluator.upload

import java.nio.file.Path
import com.wine_evaluator.wine_evaluator.ingestion.WineListSource

object FileValidator {
    fun detectSource(contentType: String): WineListSource {
        return when {
            contentType == "application/pdf" ->
                WineListSource.PDF_TEXT
            contentType.startsWith("image/") ->
                WineListSource.OCR
            else ->
                throw IllegalArgumentException("Unsupported content type: $contentType")
        }
    }
}