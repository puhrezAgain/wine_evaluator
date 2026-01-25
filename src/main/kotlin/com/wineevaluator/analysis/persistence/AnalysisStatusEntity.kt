package com.wineevaluator.analysis.persistence

import com.wineevaluator.analysis.model.AnalysisStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "analysis_status")
class AnalysisStatusEntity(
    @Id
    val id: UUID,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AnalysisStatus = AnalysisStatus.PENDING,
    @Column
    val error: String? = null,
    @Column
    val filepath: String? = null,
)
