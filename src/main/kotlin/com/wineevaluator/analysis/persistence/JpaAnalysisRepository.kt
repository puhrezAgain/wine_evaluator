package com.wineevaluator.analysis.persistence

import org.springframework.stereotype.Component
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisStatus
import com.wineevaluator.analysis.model.AnalysisRecord
import java.util.UUID
import java.nio.file.Path

@Component
class JpaAnalysisRepository(
    private val jpa: AnalysisStatusRepository
): AnalysisRepository{
    override fun create(id: AnalysisId, filepath: Path?): AnalysisId {
        jpa.save(
            AnalysisStatusEntity(id = id.value, filepath = filepath.toString())
        )
        return id
    }

    override fun markDone(id: AnalysisId) {
        jpa.UpdateStatus(id.value, AnalysisStatus.DONE)
    }

    override fun markFailed(id: AnalysisId, error: String) {
        jpa.UpdateStatus(id.value, AnalysisStatus.FAILED, error)
    }

    override fun find(id: AnalysisId): AnalysisRecord? {
        return jpa.findById(id.value)
            .map{ it.toRecord() }
            .orElse(null)
    }
}