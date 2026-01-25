package com.wineevaluator.analysis.queue

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.document.DocumentProcessingPipeline
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.persistence.JpaAnalysisRepository



@Component
class AsyncAnalysisWorker (
    private val pipeline: DocumentProcessingPipeline,
    private val analysisRepository: JpaAnalysisRepository,
)  : DocumentProcessingQueue {

    @Async
    @Transactional
    override fun enqueue(documentFile: DocumentFile) {
        val id = AnalysisId(documentFile.id.value)

        try {
            println("QUEUE QUEUE QUEUE")
            pipeline.process(documentFile)
            analysisRepository.markDone(id)
            println("DONE DONE DONE")
        } catch (e: Exception) {
            println("FAIL FAIL FAIL")
            analysisRepository.markFailed(id, e.message ?: "Processing failed")
        }
    }

}