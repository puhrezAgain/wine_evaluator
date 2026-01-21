package com.wine_evaluator.wine_evaluator.ingestion

import java.nio.file.Path

enum class WineListSource {
    PDF_TEXT,
    OCR
}

private val IMAGE_EXTENSIONS = setOf(
    "jpg",
    "jpeg",
    "png",
    "webp",
    "tiff",
    "tif",
    "bmp",
    "gif"
)

fun detectSource(path: Path): WineListSource {
    val extension = path.fileName
        ?.toString()
        ?.substringAfterLast('.', missingDelimiterValue = "")
        ?.lowercase()

    return when {
        extension == "pdf" ->
            WineListSource.PDF_TEXT
        extension in IMAGE_EXTENSIONS ->
            WineListSource.OCR
        extension.isNullOrEmpty() ->
            throw IllegalArgumentException("File has no extension: $path")
        else ->
            throw IllegalArgumentException("Unsupported file type: $extension")
    }
}