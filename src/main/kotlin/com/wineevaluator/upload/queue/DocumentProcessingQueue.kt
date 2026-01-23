package com.wineevaluator.upload.queue

import org.springframework.stereotype.Component
import com.wineevaluator.document.model.DocumentFile
import java.util.UUID

@Component
interface DocumentProcessingQueue {
    fun enqueue(DocumentFile: DocumentFile)
}