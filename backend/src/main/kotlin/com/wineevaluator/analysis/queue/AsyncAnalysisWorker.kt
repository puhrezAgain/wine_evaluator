package com.wineevaluator.analysis.queue

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.persistence.JpaAnalysisRepository
import com.wineevaluator.common.error.DomainException
import com.wineevaluator.document.DocumentProcessingPipeline
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.document.model.PriceSignal
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val log = LoggerFactory.getLogger(AsyncAnalysisWorker::class.java)

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

        log.info("analysis.processing.start id={}", id.value)

        try {
            pipeline.process(documentFile)
            analysisRepository.markDone(id)
            log.info("analysis.processing.completed id={}", id.value)
        } catch (e: DomainException) {
            log.warn("analysis.processing.failed id={} reason={}", id.value, e.message)

            analysisRepository.markFailed(id, e.message ?: "Processing failed")
        } catch (e: Exception) {
            log.error("analysis.processing.crashed id={} reason={}", id.value, e.message)

            analysisRepository.markFailed(id, e.message ?: "Internal processing error")
        }
    }

    override fun processImmediate(documentFile: DocumentFile): List<PriceSignal> {
        return pipeline.fileToSignals(documentFile)
    }
}
