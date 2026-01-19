package com.wine_evaluator.wine_evaluator.ocr
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.InputConfig
import com.google.cloud.vision.v1.AnnotateFileRequest
import com.google.cloud.vision.v1.BatchAnnotateFilesRequest
import com.google.cloud.vision.v1.TextAnnotation
import com.google.protobuf.ByteString
import com.wine_evaluator.wine_evaluator.ocr.OcrLine
import java.nio.file.Files
import java.nio.file.Path
import kotlin.comparisons.compareBy

class GoogleVisionOcrService {
    fun extractLines(path: Path): List<String> {
        return if (isPdf(path)) {
            extractTextFromPdf(path)
        } else {
            extractTextFromImage(path)
        }
    }

    private fun extractTextFromPdf(pdfPath: Path): List<String> {
        val pdfBytes = Files.readAllBytes(pdfPath)

        if (pdfBytes.isEmpty()) {
            throw IllegalArgumentException("PDF file is empty or unreadable")
        }

        val inputConfig = InputConfig.newBuilder()
            .setMimeType("application/pdf")
            .setContent(ByteString.copyFrom(pdfBytes))
            .build()

        val feature = Feature.newBuilder()
            .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
            .build()

        val request = AnnotateFileRequest.newBuilder()
            .setInputConfig(inputConfig)
            .addFeatures(feature)
            .build()

        val batchRequest = BatchAnnotateFilesRequest.newBuilder()
                    .addRequests(request)   
                    .build()

        val extractedLines = ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateFiles(batchRequest)

            val fileResponse = response.responsesList.firstOrNull()
                ?: return@use emptyList()

            if (fileResponse.hasError()) {
                throw RuntimeException("PDF OCR error: ${fileResponse.error.message}")
            }

            fileResponse.responsesList.flatMap { 
                extractParagraphLines(it.fullTextAnnotation) 
            }
        }

        return extractedLines.map { it.text }
    }

    private fun extractTextFromImage(imagePath: Path): List<String> {        
        val imageBytes = Files.readAllBytes(imagePath)

        if (imageBytes.isEmpty()) {
            throw IllegalArgumentException("Image file is empty or unreadable")
        }

        val image = Image.newBuilder()
            .setContent(ByteString.copyFrom(imageBytes))
            .build()
        
        val feature = Feature.newBuilder()
            .setType(Feature.Type.TEXT_DETECTION)
            .build()

        val request = AnnotateImageRequest.newBuilder()
            .addFeatures(feature)
            .setImage(image)
            .build()
        
        val extractedLines = ImageAnnotatorClient.create().use { client ->
            val response = client.batchAnnotateImages(listOf(request))
            val annotation = response.responsesList.firstOrNull()
                ?: return@use emptyList()

            if (annotation.hasError()) {
                throw RuntimeException("OCR error: ${annotation.error.message}")            
            }

            extractParagraphLines(annotation.fullTextAnnotation)
        }

        return mergeLinesVisually(extractedLines)
    }
    private fun extractParagraphLines(text: TextAnnotation): List<OcrLine> {
        return text.pagesList
            .flatMap{ it.blocksList }
            .flatMap { it.paragraphsList }
            .flatMap { it.wordsList }
            .mapNotNull { word ->
                val wordText = word.symbolsList
                    .joinToString("") { it.text }
                    .trim()

                if (wordText.isEmpty()) return@mapNotNull null
                val bbox = word.boundingBox.verticesList

                val centerX = bbox.map { it.x }.average().toFloat()
                val baselineY = bbox.map { it.y }.maxOrNull()!!.toFloat()
                OcrLine(wordText, centerX, baselineY, word.confidence)
             }
    }

    private fun mergeLinesVisually(lines: List<OcrLine>): List<String> {
        if (lines.isEmpty()) return emptyList()

        val sorted = lines.sortedWith (compareBy<OcrLine> { it.baselineY } )
        val result = mutableListOf<MutableList<OcrLine>>()

        for (line in sorted)  {
            val currentRow = result.lastOrNull()

            if (currentRow == null) {
                result.add(mutableListOf(line))
                continue
            }

            val gap = kotlin.math.abs(line.baselineY - currentRow.first().baselineY)

            if (gap < 20) {
                currentRow.add(line)
            } else {
                result.add(mutableListOf(line))
            }
        }
        return result.map{ row ->
            row
                .sortedBy { it.centerX }
                .joinToString(" ") { it.text } 
        }
    }

    private fun isPdf(path: Path): Boolean = 
        path.fileName.toString().lowercase().endsWith(".pdf")
}