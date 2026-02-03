package com.wineevaluator.analysis

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisRecord
import com.wineevaluator.analysis.persistence.AnalysisRepository
import com.wineevaluator.analysis.queue.DocumentProcessingQueue
import com.wineevaluator.common.error.ValidationException
import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.upload.storage.UploadStorage
import java.nio.file.Files
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

private val log = LoggerFactory.getLogger(StartAnalysisPipeline::class.java)

@Component
class StartAnalysisPipeline(
        private val storage: UploadStorage,
        private val repo: AnalysisRepository,
        private val queue: DocumentProcessingQueue
) {
    fun execute(file: MultipartFile): AnalysisRecord {
        validateFile(file)

        val id = AnalysisId.new()

        val document =
                storage.store(
                        file.inputStream,
                        id.toUploadId(),
                        file.originalFilename ?: "upload",
                )

        repo.create(id, document.path)

        queue.enqueue(document)

        return AnalysisRecord.started(id)
    }

    fun executeDiagnostic(file: MultipartFile): List<PriceSignal> {
        validateFile(file)
        val id = AnalysisId.new()
        val document =
                storage.storeTemp(
                        file.inputStream,
                        id.toUploadId(),
                        file.originalFilename ?: "upload",
                )

        return try {
            queue.processImmediate(document)
        } finally {
            runCatching { Files.deleteIfExists(document.path) }.onFailure {
                log.warn("Failed to delete temp document {}", document.path, it)
            }
        }
    }
    private fun validateFile(file: MultipartFile) {
        val type = file.contentType ?: throw ValidationException("Missing Content-Type head")
        if (!(type.startsWith("image/") || type == MediaType.APPLICATION_PDF_VALUE)) {
            throw ValidationException("Only images or PDFs allowed")
        }

        if (file.isEmpty()) {
            throw ValidationException("Empty file")
        }
    }
}
