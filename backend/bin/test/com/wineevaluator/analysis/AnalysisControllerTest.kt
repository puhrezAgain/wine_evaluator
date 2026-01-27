package com.wineevaluator.analysis

import com.fasterxml.jackson.databind.ObjectMapper
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.common.error.NotFoundException
import com.wineevaluator.common.http.ApiExceptionHandler
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.wine.model.WineQueryResponse
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class AnalysisControllerTest {
    private lateinit var mockMvc: MockMvc

    private val analyzer = mockk<Analyzer>()
    private val analysisReader = mockk<AnalysisReader>()
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        val controller = AnalysisController(analyzer, analysisReader)

        mockMvc =
                MockMvcBuilders.standaloneSetup(controller)
                        .setControllerAdvice(ApiExceptionHandler())
                        .build()
    }

    @Test
    fun `POST analysis query returns 200`() {
        val query = WineQueryRequest("Viña Tondonía", 48f)
        val response =
                AnalysisResponse.AnalysisImmediate(
                        results =
                                WineQueryResponse(
                                        original = query.wine,
                                        queryPrice = query.price,
                                        matches = emptyList()
                                )
                )
        every { analyzer.query(query) } returns response

        val content = objectMapper.writeValueAsString(query)

        post("/analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content)
                .let(mockMvc::perform)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.results").exists())
                .andExpect(jsonPath("$.results.original").value("Viña Tondonía"))
    }

    @Test
    fun `GET analysis returns 404 when not found`() {
        val id = UUID.randomUUID()

        every { analysisReader.getAnalysis(AnalysisId(id)) } throws
                NotFoundException("Analysis not found")

        get("/analysis/$id")
                .let(mockMvc::perform)
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
    }
}
