package com.wineevaluator.analysis.queue

import org.springframework.stereotype.Component
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.analysis.model.AnalysisId

@Component
interface DocumentProcessingQueue {
    fun enqueue(documentFile: DocumentFile)
}