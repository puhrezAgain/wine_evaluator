package com.wineevaluator.analysis.queue

import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.document.model.PriceSignal

interface DocumentProcessingQueue {
    fun enqueue(documentFile: DocumentFile)
    fun processImmediate(documentFile: DocumentFile): List<PriceSignal>
}
