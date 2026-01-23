package com.wineevaluator.document.ingestion

import com.wineevaluator.document.ocr.parseImage
import com.wineevaluator.document.model.DocumentFile
import org.springframework.stereotype.Component

import java.util.UUID
import java.nio.file.Path

@Component
class DocumentParser{
    fun parse(
        file: DocumentFile,
    ): List<String> {
        return when (determineWineListSource(file.path)) {
            WineListSource.OCR ->
                parseImage(file.path)
            WineListSource.PDF_TEXT ->
                parsePDF(file.path)
        }
    }
}