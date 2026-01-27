package com.wineevaluator.analysis.model

import com.wineevaluator.wine.model.WineQueryResponse

sealed interface AnalysisResponse {
        data class AnalysisStarted(
                val record: AnalysisRecord,
        ) : AnalysisResponse

        data class AnalysisImmediate(
                val results: WineQueryResponse,
        ) : AnalysisResponse
}
