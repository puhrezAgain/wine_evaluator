package com.wineevaluator.analysis.persistence

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisRecord
import java.nio.file.Path

interface AnalysisRepository {
    fun create(id: AnalysisId, filepath: Path? = null): AnalysisId
    fun markDone(id: AnalysisId)
    fun markFailed(id: AnalysisId, error: String)
    fun find(id: AnalysisId): AnalysisRecord?
}