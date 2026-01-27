package com.wineevaluator.document.persistence

import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface DocumentPriceSignalRepository : JpaRepository<DocumentPriceSignalEntity, UUID> {
    @Query(
            """
        SELECT DISTINCT dps
        FROM DocumentPriceSignalEntity dps
        JOIN dps.identityTokens t
        WHERE t in :tokens
        """,
    )
    fun findCandidatesByTokens(
            @Param("tokens") tokens: Set<String>,
    ): List<DocumentPriceSignalEntity>

    fun findByUploadId(uploadId: UUID): List<DocumentPriceSignalEntity>
}
