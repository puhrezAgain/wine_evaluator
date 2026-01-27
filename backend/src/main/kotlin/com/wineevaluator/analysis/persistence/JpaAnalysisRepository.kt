package com.wineevaluator.analysis.persistence

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisRecord
import com.wineevaluator.analysis.model.AnalysisStatus
import java.nio.file.Path
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class JpaAnalysisRepository(
        private val jpa: AnalysisStatusRepository,
) : AnalysisRepository {
    override fun create(
            id: AnalysisId,
            filepath: Path?,
    ): AnalysisId {
        jpa.save(
                AnalysisStatusEntity(id = id.value, filepath = filepath?.toString()),
        )
        return id
    }

    override fun markDone(id: AnalysisId) {
        jpa.updateStatus(id.value, AnalysisStatus.DONE)
    }

    override fun markFailed(
            id: AnalysisId,
            error: String,
    ) {
        jpa.updateStatus(id.value, AnalysisStatus.FAILED, error)
    }

    override fun find(id: AnalysisId): AnalysisRecord? = jpa.findByIdOrNull(id.value)?.toRecord()
}
