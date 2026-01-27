package com.wineevaluator.analysis

import io.mockk.*
import com.wineevaluator.analysis.queue.AsyncAnalysisWorker
import com.wineevaluator.analysis.persistence.JpaAnalysisRepository
import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.analysis.model.AnalysisStatus
import com.wineevaluator.wine.WineQueryHandler
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.common.error.ValidationException
import com.wineevaluator.common.value.UploadId
import com.wineevaluator.analysis.model.AnalysisRecord
import com.wineevaluator.analysis.model.AnalysisId

import org.springframework.web.multipart.MultipartFile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class AnalyzerTest {
    private val pipeline = mockk<StartAnalysisPipeline>()
    private val wineQuerier = mockk<WineQueryHandler>()

    private lateinit var analyzer: Analyzer

    @BeforeEach
    fun setUp() {
        analyzer = Analyzer(
            wineQuerier = wineQuerier,
            pipeline = pipeline
        )
    }

    @Test
    fun `query delegates to WineQueryHandler and returns immediate response`() {
        val request = WineQueryRequest("Viña Tondonía", 48f)

        every {
            wineQuerier.query(request)
        } returns emptyList()

        val response = analyzer.query(request)

        val immediate = response as AnalysisResponse.AnalysisImmediate

        assertEquals(request.wine, immediate.results.original)
        assertEquals(request.price, immediate.results.queryPrice)

        verify(exactly = 1) {
            wineQuerier.query(request)
        }
    }
    @Test
    fun `start executes pipeline`() {
        val file = mockk<MultipartFile>(relaxed = true)
        val record = AnalysisRecord.started(AnalysisId.new())
        every { pipeline.execute(file) } returns record

        val response = analyzer.start(file)

        assertTrue(response is AnalysisResponse.AnalysisStarted)

        verify(exactly = 1) {
            pipeline.execute(file)
        }
    }
}