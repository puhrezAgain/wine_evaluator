package com.wineevaluator.analysis

import org.springframework.stereotype.Component
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisStatus
import com.wineevaluator.analysis.model.AnalysisResultView
import com.wineevaluator.analysis.persistence.AnalysisRepository


import com.wineevaluator.wine.WineQueryHandler

@Component
class AnalysisReader(
    private val analysisRepository: AnalysisRepository,
    private val wineQuerier: WineQueryHandler
) {
    fun getAnalysis(id: AnalysisId): AnalysisResultView {
        val record = analysisRepository.find(id)
            ?: throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Analysis not found: $id"
        )

        return when (record.status) {
            AnalysisStatus.PENDING ->
                AnalysisResultView.pending(id)

            AnalysisStatus.FAILED ->
                AnalysisResultView.failed(id, record.error)

            AnalysisStatus.DONE -> {
                val results = wineQuerier.queryUpload(UploadId(id.value))

                AnalysisResultView.done(id, results)
            }


        }
    }
}