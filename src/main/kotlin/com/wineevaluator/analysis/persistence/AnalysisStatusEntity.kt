
package com.wineevaluator.analysis.persistence

import org.hibernate.annotations.UuidGenerator
import jakarta.persistence.*
import java.util.UUID
import java.nio.file.Path

import com.wineevaluator.analysis.model.AnalysisStatus

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
    val filepath: String? = null
)

