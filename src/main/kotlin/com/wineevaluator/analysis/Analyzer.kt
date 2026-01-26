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
import com.wineevaluator.common.error.ValidationException
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
            throw ValidationException("Wine name must not be empty")
        }

        if (query.price <= 0) {
            throw ValidationException("Price must be positive")
        }
    }

    private fun validateFile(file: MultipartFile) {
        val type =
            file.contentType ?: throw ValidationException("Missing Content-Type head")
        if (!(type.startsWith("image/") || type == MediaType.APPLICATION_PDF_VALUE)) {
            throw ValidationException("Only images or PDFs allowed")
        }

        if (file.isEmpty()) {
            throw ValidationException("Empty file")
        }
    }
}
