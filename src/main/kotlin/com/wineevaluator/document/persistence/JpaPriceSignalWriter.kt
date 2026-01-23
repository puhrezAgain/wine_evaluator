package com.wineevaluator.document.persistence

import org.springframework.stereotype.Component
import com.wineevaluator.document.model.PriceSignal

@Component
class JpaPriceSignalWriter(
    private val repository: JpaPriceSignalRepository
) : PriceSignalWriter {

    override fun write(signals: List<PriceSignal>) {
        val entities = signals.map { it.toEntity() }
        repository.saveAll(entities)
    }

}