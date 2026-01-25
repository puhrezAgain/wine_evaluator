package com.wineevaluator.analysis.model

import com.wineevaluator.analysis.model.AnalysisStatus
import java.util.UUID

@JvmInline
value class AnalysisId(val value: UUID)

enum class AnalysisStatus {
    PENDING,
    DONE,
    FAILED
}

data class AnalysisRecord(
    val id: AnalysisId,
    val status: AnalysisStatus = AnalysisStatus.PENDING,
    val error: String? = null
)