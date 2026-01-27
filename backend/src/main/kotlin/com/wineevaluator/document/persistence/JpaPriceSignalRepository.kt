package com.wineevaluator.document.persistence

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.PriceSignal
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class JpaPriceSignalRepository(
        private val repository: DocumentPriceSignalRepository,
) : PriceSignalRepository {
    @Transactional
    override fun write(signals: List<PriceSignal>) {
        val entities = signals.map { it.toEntity() }
        repository.saveAll(entities)
    }

    override fun findCandidatesByTokens(tokens: Set<String>): List<PriceSignal> =
            repository.findCandidatesByTokens(tokens).map { it.toDomain() }

    override fun findByUploadId(id: UploadId): List<PriceSignal> =
            repository.findByUploadId(id.value).map { it.toDomain() }
}
