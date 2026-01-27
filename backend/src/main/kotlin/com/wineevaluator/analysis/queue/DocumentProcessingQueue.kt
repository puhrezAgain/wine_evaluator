package com.wineevaluator.analysis.queue

import com.wineevaluator.document.model.DocumentFile

interface DocumentProcessingQueue {
    fun enqueue(documentFile: DocumentFile)
}
