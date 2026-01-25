package com.wineevaluator.analysis.model

import com.wineevaluator.wine.model.WineQueryResponse

data class AnalysisResponse(
    val record: AnalysisRecord? = null,
    val results: WineQueryResponse? = null
)

