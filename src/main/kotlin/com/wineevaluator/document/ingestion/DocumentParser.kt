package com.wineevaluator.document.ingestion

import com.wineevaluator.document.ocr.parseImage
import com.wineevaluator.upload.UploadedFile
import org.springframework.stereotype.Component

import java.util.UUID
import java.nio.file.Path

@Component
class DocumentParser{
    fun parse(
        uploadedFile: UploadedFile,
    ): List<String> {
        return when (determineWineListSource(uploadedFile.path)) {
            WineListSource.OCR ->
                parseImage(uploadedFile.path)
            WineListSource.PDF_TEXT ->
                parsePDF(uploadedFile.path)
        }
    }
}