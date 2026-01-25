package com.wineevaluator.document.persistence

import org.springframework.stereotype.Component

import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.common.value.UploadId

@Component
class JpaPriceSignalRepository(
    private val repository: DocumentPriceSignalRepository
) : PriceSignalRepository {
    override fun write(signals: List<PriceSignal>) {
        val entities = signals.map { it.toEntity() }
        repository.saveAll(entities)
    }
    override fun findCandidatesByTokens(tokens: Set<String>): List<PriceSignal> =
        repository.findCandidatesByTokens(tokens)
            .map{ it.toDomain() }

    override fun findByUploadId(id: UploadId): List<PriceSignal> =
        repository.findByUploadId(id.value)
            .map{ it.toDomain() }
}