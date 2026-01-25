package com.wineevaluator.analysis.queue

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.document.model.DocumentFile
import org.springframework.stereotype.Component

interface DocumentProcessingQueue {
    fun enqueue(documentFile: DocumentFile)
}
