package com.wineevaluator.document.ingestion

import com.wineevaluator.common.error.ValidationException
import java.nio.file.Path

enum class WineListSource {
    PDF_TEXT,
    OCR,
}

private val IMAGE_EXTENSIONS =
        setOf(
                "jpg",
                "jpeg",
                "png",
                "webp",
                "tiff",
                "tif",
                "bmp",
                "gif",
        )

fun determineWineListSource(path: Path): WineListSource {
    val extension =
            path.fileName
                    ?.toString()
                    ?.substringAfterLast('.', missingDelimiterValue = "")
                    ?.lowercase()

    return when {
        extension == "pdf" -> {
            WineListSource.PDF_TEXT
        }
        extension in IMAGE_EXTENSIONS -> {
            WineListSource.OCR
        }
        extension.isNullOrEmpty() -> {
            throw ValidationException("File has no extension: $path")
        }
        else -> {
            throw ValidationException("Unsupported file type: $extension")
        }
    }
}
