package com.wineevaluator.analysis

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import io.mockk.mockk
import io.mockk.every
import com.wineevaluator.analysis.Analyzer
import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.common.http.ApiExceptionHandler
import com.wineevaluator.common.error.NotFoundException
import com.wineevaluator.wine.model.WineQueryRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.UUID

class AnalysisControllerTest {
    private lateinit var mockMvc: MockMvc

    private val analyzer = mockk<Analyzer>()
    private val analysisReader = mockk<AnalysisReader>()
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        val controller = AnalysisController(analyzer, analysisReader)

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(ApiExceptionHandler())
            .build()
    }

    @Test
    fun `POST analysis query returns 200`() {
        every { analyzer.query(any()) } returns
            AnalysisResponse.AnalysisImmediate(
                results = mockk(relaxed = true)
            )
        val content = objectMapper.writeValueAsString(
            WineQueryRequest("Viña Tondonía", 48f)
        )
        post("/analysis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(content)
            .let(mockMvc::perform)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.results").exists())
    }

    @Test
    fun `GET analysis returns 404 when not found`() {
        val id = UUID.randomUUID()

        every {
            analysisReader.getAnalysis(AnalysisId(id))
        } throws NotFoundException("Analysis not found")

        get("/analysis/$id")
            .let(mockMvc::perform)
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("NOT_FOUND"))
    }

}