package com.wineevaluator.analysis.persistence

import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID
import com.wineevaluator.analysis.model.AnalysisStatus

@Repository
interface AnalysisStatusRepository:
    JpaRepository<AnalysisStatusEntity, UUID> {

    @Modifying
    @Query("""
    update AnalysisStatusEntity
    set status = :status, error = :error
    where id = :id
    """)
    fun UpdateStatus(
        @Param("id") id: UUID,
        @Param("status") status: AnalysisStatus,
        @Param("error") error: String? = null,
    )
}