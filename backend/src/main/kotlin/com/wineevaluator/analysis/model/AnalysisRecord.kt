package com.wineevaluator.analysis.model

import com.wineevaluator.common.value.UploadId
import java.util.UUID

@JvmInline
value class AnalysisId(
        val value: UUID,
) {
    fun toUploadId() = UploadId(value)

    companion object {
        fun new() = AnalysisId(UUID.randomUUID())
    }
}

enum class AnalysisStatus {
    PENDING,
    DONE,
    FAILED,
}

data class AnalysisRecord(
        val id: AnalysisId,
        val status: AnalysisStatus = AnalysisStatus.PENDING,
        val error: String? = null,
) {
    companion object {
        fun started(id: AnalysisId): AnalysisRecord = AnalysisRecord(id)
    }
}
