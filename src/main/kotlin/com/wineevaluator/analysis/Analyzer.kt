package com.wineevaluator.analysis

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ResponseStatusException
import com.wineevaluator.upload.storage.LocalStorage
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.wine.model.WineQueryResponse
import com.wineevaluator.wine.WineQueryHandler
import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisStatus
import com.wineevaluator.analysis.model.AnalysisRecord
import com.wineevaluator.analysis.persistence.JpaAnalysisRepository
import com.wineevaluator.analysis.queue.AsyncAnalysisWorker
import com.wineevaluator.common.value.UploadId

import java.util.UUID

@Service
class Analyzer(
    private val worker: AsyncAnalysisWorker,
    private val repo: JpaAnalysisRepository,
    private val wineQuerier: WineQueryHandler,
    private val storage: LocalStorage
) {
    fun start(file: MultipartFile): AnalysisResponse {
        validateFile(file)

        val id = AnalysisId(UUID.randomUUID())

        val document = storage.store(
            file.inputStream,
            UploadId(id.value),
            file.originalFilename ?: "upload")

        repo.create(id, document.path)

        worker.enqueue(document)

        return AnalysisResponse(record = AnalysisRecord(id))
    }

    fun query(query: WineQueryRequest): AnalysisResponse {
        val matches = wineQuerier.query(query.wine, query.price.toInt())

        return AnalysisResponse(
            results = WineQueryResponse(query.wine, query.price, matches)
        )
    }

    private fun validateFile(file: MultipartFile) {
        val type = file.contentType ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Missing Content-Type"
        )

        if (!(type.startsWith("image/") || type == MediaType.APPLICATION_PDF_VALUE)) {
            throw ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Only images or PDFs allowed"
        )
        }

        if (file.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Empty file"
            )
        }
    }

}