package com.wineevaluator.analysis.queue

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.persistence.JpaAnalysisRepository
import com.wineevaluator.document.DocumentProcessingPipeline
import com.wineevaluator.document.model.DocumentFile
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import javax.print.Doc

@Component
class AsyncAnalysisWorker(
    private val pipeline: DocumentProcessingPipeline,
    private val analysisRepository: JpaAnalysisRepository,
) : DocumentProcessingQueue {
    @Async
    override fun enqueue(documentFile: DocumentFile) {
        process(documentFile)
    }

    @Transactional
    internal fun process(documentFile: DocumentFile) {
        val id = AnalysisId(documentFile.id.value)

        try {
            pipeline.process(documentFile)
            analysisRepository.markDone(id)
        } catch (e: Exception) {
            analysisRepository.markFailed(id, e.message ?: "Processing failed")
        }
    }
}
