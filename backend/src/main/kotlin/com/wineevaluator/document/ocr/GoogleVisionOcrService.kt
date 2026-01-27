package com.wineevaluator.document.ocr

import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.TextAnnotation
import com.google.protobuf.ByteString
import com.wineevaluator.common.error.ProcessingException
import com.wineevaluator.common.error.ValidationException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.comparisons.compareBy

data class OcrLine(
        val text: String,
        val centerX: Float,
        val baselineY: Float,
        val confidence: Float,
)

private const val MAX_VERTICAL_GAP = 20

fun parseImage(path: Path): List<String> {
    val imageBytes = Files.readAllBytes(path)

    if (imageBytes.isEmpty()) {
        throw ValidationException("Image file is empty or unreadable")
    }

    val image = Image.newBuilder().setContent(ByteString.copyFrom(imageBytes)).build()

    val feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()

    val request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build()

    val extractedLines =
            ImageAnnotatorClient.create().use { client ->
                val response = client.batchAnnotateImages(listOf(request))
                val annotation = response.responsesList.firstOrNull() ?: return@use emptyList()

                if (annotation.hasError()) {
                    throw ProcessingException("OCR error: ${annotation.error.message}")
                }

                extractImageLines(annotation.fullTextAnnotation)
            }

    return mergeLinesVisually(extractedLines)
}

private fun extractImageLines(text: TextAnnotation): List<OcrLine> {
    return text.pagesList
            .flatMap { it.blocksList }
            .flatMap { it.paragraphsList }
            .flatMap { it.wordsList }
            .mapNotNull { word ->
                val wordText = word.symbolsList.joinToString("") { it.text }.trim()

                if (wordText.isEmpty()) return@mapNotNull null
                val bbox = word.boundingBox.verticesList

                val centerX = bbox.map { it.x }.average().toFloat()
                val baselineY = bbox.map { it.y }.maxOrNull()!!.toFloat()
                OcrLine(wordText, centerX, baselineY, word.confidence)
            }
}

internal fun mergeLinesVisually(lines: List<OcrLine>): List<String> {
    if (lines.isEmpty()) return emptyList()

    val sorted = lines.sortedWith(compareBy<OcrLine> { it.baselineY })
    val result = mutableListOf<MutableList<OcrLine>>()

    for (line in sorted) {
        val currentRow = result.lastOrNull()

        if (currentRow == null) {
            result.add(mutableListOf(line))
            continue
        }

        val gap = kotlin.math.abs(line.baselineY - currentRow.first().baselineY)

        if (gap < MAX_VERTICAL_GAP) {
            currentRow.add(line)
        } else {
            result.add(mutableListOf(line))
        }
    }

    return result.map { row -> row.sortedBy { it.centerX }.joinToString(" ") { it.text } }
}
