package com.wineevaluator.analysis

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.wineevaluator.analysis.persistence.AnalysisRepository
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisRecord
import com.wineevaluator.analysis.model.AnalysisStatus
import com.wineevaluator.analysis.model.AnalysisResultView
import com.wineevaluator.wine.WineQueryHandler
import com.wineevaluator.wine.model.WineMatch
import com.wineevaluator.common.error.NotFoundException
import com.wineevaluator.common.value.UploadId
import java.util.UUID

class AnalysisReaderTest {
    private val repo = mockk<AnalysisRepository>()
    private val wineQuerier = mockk<WineQueryHandler>()

    private lateinit var reader: AnalysisReader

    @BeforeEach
    fun setUp() {
        reader = AnalysisReader(
            analysisRepository = repo,
            wineQuerier = wineQuerier
        )
    }

    @Test
    fun `throws NotFoundException when analysis not found`() {
        val id = AnalysisId(UUID.randomUUID())

        every { repo.find(id) } returns null

        assertThrows(NotFoundException::class.java) {
            reader.getAnalysis(id)
        }
    }

    @Test
    fun `throws Pending view when status is PENDING`() {
        val id = AnalysisId(UUID.randomUUID())

        every { repo.find(id) } returns
            AnalysisRecord(id, AnalysisStatus.PENDING)

        val view = reader.getAnalysis(id)

        assertTrue(view is AnalysisResultView.Pending)
        assertEquals(id, view.id)

        verify { wineQuerier wasNot Called}

    }

    @Test
    fun `returns Failed view when status is FAILED`() {
        val id = AnalysisId(UUID.randomUUID())

        every { repo.find(id) } returns
            AnalysisRecord(
                id = id,
                status = AnalysisStatus.FAILED,
                error = "OCR failed"
            )

        val view = reader.getAnalysis(id)

        val failed = view as AnalysisResultView.Failed

        assertEquals(id, failed.id)
        assertEquals("OCR failed", failed.error)

        verify { wineQuerier wasNot Called }
    }

    @Test
    fun `retruns Done view and queries by uploadId when status is DONE`() {
        val id = AnalysisId(UUID.randomUUID())
        val uploadId = id.toUploadId()

        val matches = listOf(
            WineMatch(
                signalId = UUID.randomUUID(),
                jaccard = 0.82,
                price = 48,
                referencePrice = 32,
                delta = 16,
                deltaPercent = 50.0,
                matchTokens = setOf("VINA", "TONDONIA"),
                tokens = setOf("VINA", "TONDONIA", "RESERVA")
            )
        )
        every { repo.find(id) } returns
            AnalysisRecord(id, AnalysisStatus.DONE)

        every {
            wineQuerier.queryByUploadId(uploadId)
        } returns matches

        val view = reader.getAnalysis(id)

        val done = view as AnalysisResultView.Done

        assertEquals(id, done.id)
        assertEquals(matches, done.results)

        verify(exactly = 1) {
            wineQuerier.queryByUploadId(uploadId)
        }
    }
}