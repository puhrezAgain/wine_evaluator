package com.wineevaluator.upload.queue

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.document.DocumentProcessingPipeline


@Component
class AsyncDocumentProcessingQueue (
    private val pipeline:DocumentProcessingPipeline
)  : DocumentProcessingQueue {

    @Async
    override fun enqueue(DocumentFile: DocumentFile) {
        pipeline.process(DocumentFile)
    }

}