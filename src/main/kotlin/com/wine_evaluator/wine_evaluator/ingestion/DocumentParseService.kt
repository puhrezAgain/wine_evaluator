package com.wine_evaluator.wine_evaluator.ingestion

import com.wine_evaluator.wine_evaluator.persistence.DocumentPriceSignalRepository
import com.wine_evaluator.wine_evaluator.persistence.DocumentPriceSignalEntity
import com.wine_evaluator.wine_evaluator.interpretation.processRow
import com.wine_evaluator.wine_evaluator.ocr.parseImage
import org.springframework.stereotype.Service

import java.util.UUID
import java.nio.file.Path

@Service
class DocumentParseService(
    private val repository: DocumentPriceSignalRepository
){


    fun parseAndPersist(
        uploadId: UUID,
        path: Path
    ){
        val lines = when (determineWineListSource(path)) {
            WineListSource.OCR ->
                parseImage(path)
            WineListSource.PDF_TEXT ->
                parsePDF(path)
        }
        lines.forEach{ line ->

            val row = processRow(line) ?: return@forEach
            repository.save(
                DocumentPriceSignalEntity(
                    uploadId = uploadId,
                    identityTokens = row.identityTokens,
                    priceHints = row.priceTokens,
                    rawRow = line
                )
            )

        }
    }
}