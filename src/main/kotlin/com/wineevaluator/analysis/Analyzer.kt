package com.wineevaluator.analysis

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisRecord
import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.analysis.persistence.JpaAnalysisRepository
import com.wineevaluator.analysis.queue.AsyncAnalysisWorker
import com.wineevaluator.upload.storage.LocalStorage
import com.wineevaluator.wine.WineQueryHandler
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.wine.model.WineQueryResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@Service
class Analyzer(
    private val worker: AsyncAnalysisWorker,
    private val repo: JpaAnalysisRepository,
    private val wineQuerier: WineQueryHandler,
    private val storage: LocalStorage,
) {
    fun start(file: MultipartFile): AnalysisResponse {
        validateFile(file)

        val id = AnalysisId.new()

        val document =
            storage.store(
                file.inputStream,
                id.toUploadId(),
                file.originalFilename ?: "upload",
            )

        repo.create(id, document.path)

        worker.enqueue(document)

        return AnalysisResponse.AnalysisStarted(AnalysisRecord(id))
    }

    fun query(query: WineQueryRequest): AnalysisResponse {
        validateQuery(query)
        val results = wineQuerier.query(query.wine, query.roundedPrice())
        return AnalysisResponse.AnalysisImmediate(
            WineQueryResponse(query.wine, query.price, results),
        )
    }

    private fun validateQuery(query: WineQueryRequest) {
        if (query.wine.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No wine specified",
            )
        }

        if (query.price <= 0) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Price must be positive",
            )
        }
    }

    private fun validateFile(file: MultipartFile) {
        val type =
            file.contentType ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Missing Content-Type",
            )

        if (!(type.startsWith("image/") || type == MediaType.APPLICATION_PDF_VALUE)) {
            throw ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Only images or PDFs allowed",
            )
        }

        if (file.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Empty file",
            )
        }
    }
}
