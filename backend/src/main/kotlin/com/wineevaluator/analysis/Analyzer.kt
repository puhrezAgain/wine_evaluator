package com.wineevaluator.analysis

import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.wine.WineQueryHandler
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.wine.model.WineQueryResponse
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class Analyzer(
        private val wineQuerier: WineQueryHandler,
        private val pipeline: StartAnalysisPipeline,
) {
        fun start(file: MultipartFile): AnalysisResponse =
                AnalysisResponse.AnalysisStarted(pipeline.execute(file))

        fun query(query: WineQueryRequest): AnalysisResponse {
                val results = wineQuerier.query(query)

                return AnalysisResponse.AnalysisImmediate(
                        WineQueryResponse(query.wine, query.price, results),
                )
        }
}
