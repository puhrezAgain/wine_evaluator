package com.wineevaluator.analysis

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.wine.model.WineQueryResponse
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.analysis.model.AnalysisStatus
import com.wineevaluator.analysis.model.AnalysisRecord
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisResultView
import com.google.api.Http

import java.util.UUID

@RestController
@RequestMapping("/analysis")
class AnalysisController(
    private val analyzer: Analyzer,
    private val analysisReader: AnalysisReader
){

    @GetMapping("/{id}")
    fun getAnalysis(@PathVariable id: AnalysisId): ResponseEntity<AnalysisResultView> {
        val view = analysisReader.getAnalysis(id)

        return when (view) {
            is AnalysisResultView.Pending ->
                ResponseEntity.accepted().body(view)

            is AnalysisResultView.Failed ->
                ResponseEntity.unprocessableContent().body(view)

            is AnalysisResultView.Done ->
                ResponseEntity.ok(view)
        }
    }

    @PostMapping()
    fun analyze(
        @RequestPart(required = false) file: MultipartFile?,
        @RequestPart(required = false) query: WineQueryRequest?
    ): ResponseEntity<AnalysisResponse> {
        return when {
            query != null && file != null ->
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Input: both query and file")
            query != null -> {
                val results = analyzer.query(query)
                ResponseEntity.ok(results)
            }
            file != null -> {
                val record = analyzer.start(file)
                ResponseEntity.accepted().body(record)
            }
            else ->
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Input: no query or file")
        }
    }
}