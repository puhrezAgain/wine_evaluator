package com.wineevaluator.document.persistence

import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

@Repository
interface JpaPriceSignalRepository: JpaRepository<DocumentPriceSignalEntity, UUID> {
    @Query(
        """
        SELECT DISTINCT dps
        FROM DocumentPriceSignalEntity dps
        JOIN dps.identityTokens t
        WHERE t in :tokens
        """
    )
    fun findCandidatesByTokens(
        @Param("tokens") tokens: Set<String>
    ): List<DocumentPriceSignalEntity>
}