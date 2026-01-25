package com.wineevaluator.analysis.persistence

import com.wineevaluator.analysis.model.AnalysisStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Repository
interface AnalysisStatusRepository : JpaRepository<AnalysisStatusEntity, UUID> {
    @Transactional
    @Modifying
    @Query(
        """
    update AnalysisStatusEntity
    set status = :status, error = :error
    where id = :id
    """,
    )
    fun updateStatus(
        @Param("id") id: UUID,
        @Param("status") status: AnalysisStatus,
        @Param("error") error: String? = null,
    )
}
