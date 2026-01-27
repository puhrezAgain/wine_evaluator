package com.wineevaluator.analysis

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisResultView
import com.wineevaluator.analysis.model.AnalysisStatus
import com.wineevaluator.analysis.persistence.AnalysisRepository
import com.wineevaluator.common.error.NotFoundException
import com.wineevaluator.wine.WineQueryHandler
import org.springframework.stereotype.Component

@Component
class AnalysisReader(
        private val analysisRepository: AnalysisRepository,
        private val wineQuerier: WineQueryHandler,
) {
    fun getAnalysis(id: AnalysisId): AnalysisResultView {
        val record =
                analysisRepository.find(id) ?: throw NotFoundException("Analysis not found: $id")

        return when (record.status) {
            AnalysisStatus.PENDING -> AnalysisResultView.Pending(id)
            AnalysisStatus.FAILED -> AnalysisResultView.Failed(id, record.error)
            AnalysisStatus.DONE ->
                    wineQuerier.queryByUploadId(id.toUploadId()).let {
                        AnalysisResultView.Done(id, it)
                    }
        }
    }
}
