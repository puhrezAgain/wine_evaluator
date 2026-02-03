package com.wineevaluator.analysis.model

import com.wineevaluator.document.model.PriceSignal

data class DiagnosticResponse(val signals: List<PriceSignal>)
