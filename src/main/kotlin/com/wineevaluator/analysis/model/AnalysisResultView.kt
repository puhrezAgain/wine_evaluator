package com.wineevaluator.analysis.model

import com.wineevaluator.wine.model.WineMatch

sealed interface AnalysisResultView {
    val id: AnalysisId

    data class Pending(
        override val id: AnalysisId,
    ) : AnalysisResultView

    data class Failed(
        override val id: AnalysisId,
        val error: String?,
    ) : AnalysisResultView

    data class Done(
        override val id: AnalysisId,
        val results: List<WineMatch>,
    ) : AnalysisResultView
}
