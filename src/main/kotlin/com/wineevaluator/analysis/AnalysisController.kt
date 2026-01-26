package com.wineevaluator.analysis

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.analysis.model.AnalysisResultView
import com.wineevaluator.wine.model.WineQueryRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/analysis")
class AnalysisController(
    private val analyzer: Analyzer,
    private val analysisReader: AnalysisReader,
) {
    @GetMapping("/{id}")
    fun getAnalysis(
        @PathVariable id: AnalysisId,
    ): ResponseEntity<AnalysisResultView> {
        val view = analysisReader.getAnalysis(id)

        return when (view) {
            is AnalysisResultView.Pending -> {
                ResponseEntity.accepted().body(view)
            }

            is AnalysisResultView.Failed -> {
                ResponseEntity.unprocessableContent().body(view)
            }

            is AnalysisResultView.Done -> {
                ResponseEntity.ok(view)
            }
        }
    }
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun analyzeQuery(
        @RequestBody query: WineQueryRequest,
    ): ResponseEntity<AnalysisResponse> =
        ResponseEntity.ok(analyzer.query(query))

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun analyzeFile(
        @RequestPart file: MultipartFile,
    ): ResponseEntity<AnalysisResponse> =
        ResponseEntity.accepted().body(analyzer.start(file))
}
