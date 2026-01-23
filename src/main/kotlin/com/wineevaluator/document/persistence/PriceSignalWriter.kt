package com.wineevaluator.document.persistence
import com.wineevaluator.document.model.PriceSignal

interface PriceSignalWriter {
    fun write(signals: List<PriceSignal>)
}