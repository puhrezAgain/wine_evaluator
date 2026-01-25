package com.wineevaluator.analysis.persistence

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisRecord

fun AnalysisStatusEntity.toRecord(): AnalysisRecord =
    AnalysisRecord(
        id = AnalysisId(id),
        status = status,
        error = error,
    )
