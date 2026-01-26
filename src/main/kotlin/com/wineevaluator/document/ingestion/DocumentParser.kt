package com.wineevaluator.document.ingestion

import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.document.ocr.parseImage
import org.springframework.stereotype.Component

@Component
class DocumentParser {
    fun parse(file: DocumentFile): List<String> =
        when (determineWineListSource(file.path)) {
            WineListSource.OCR ->
                parseImage(file.path)

            WineListSource.PDF_TEXT ->
                parsePDF(file.path)
        }
}
