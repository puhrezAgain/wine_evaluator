package com.wineevaluator.analysis
import io.mockk.*
import com.wineevaluator.analysis.queue.AsyncAnalysisWorker

import com.wineevaluator.analysis.persistence.JpaAnalysisRepository
import com.wineevaluator.analysis.model.AnalysisResponse
import com.wineevaluator.analysis.model.AnalysisStatus
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.wine.WineQueryHandler
import com.wineevaluator.wine.model.WineQueryRequest
import com.wineevaluator.upload.storage.LocalStorage
import com.wineevaluator.common.error.ValidationException
import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.DocumentFile
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.time.Instant
import java.nio.file.Path
import java.util.UUID

class AnalyzerTest {

    private val worker = mockk<AsyncAnalysisWorker>(relaxed = true)
    private val repo = mockk<JpaAnalysisRepository>(relaxed = true)
    private val wineQuerier = mockk<WineQueryHandler>()
    private val storage = mockk<LocalStorage>()

    private lateinit var analyzer: Analyzer

    @BeforeEach
    fun setUp() {
        analyzer = Analyzer(
            worker = worker,
            repo = repo,
            wineQuerier = wineQuerier,
            storage = storage
        )
    }

    //
    // query tests
    //

    @Test
    fun `query throws ValidationException when wine empty`() {
        val ex = assertThrows(ValidationException::class.java) {
            analyzer.query(WineQueryRequest("", 10f))
        }

        assertEquals("Wine name must not be empty", ex.message)

    }

    @Test
    fun `query throws ValidationException when price non-positive`() {
        val ex = assertThrows(ValidationException::class.java) {
            analyzer.query(WineQueryRequest("Viña Tondonia", 0f))
        }

        assertEquals("Price must be positive", ex.message)
    }

    @Test
    fun `query delegates to WineQueryHandler and returns immeidate response`() {
        every {
            wineQuerier.query("Viña Tondonia", 48)
        } returns emptyList()

        val response = analyzer.query(
            WineQueryRequest("Viña Tondonia", 48f)
        )

        val immediate = response as AnalysisResponse.AnalysisImmediate

        assertEquals("Viña Tondonia", immediate.results.original)
        assertEquals(48f, immediate.results.queryPrice)

        verify(exactly = 1) {
            wineQuerier.query("Viña Tondonia", 48)
        }
    }

    //
    // start tests
    //

    @Test
    fun `start throws ValidationException when file empty`() {
        var file = mockk<MultipartFile> {
            every { contentType } returns MediaType.APPLICATION_PDF_VALUE
            every { isEmpty } returns true
        }

        val ex = assertThrows(ValidationException::class.java) {
            analyzer.start(file)
        }

        assertEquals("Empty file", ex.message)
    }

    @Test
    fun `start throws ValidationException on unsupported content type`() {
        val file = mockk<MultipartFile> {
            every { contentType } returns "text/plain"
            every {isEmpty } returns false
        }

        val ex = assertThrows(ValidationException::class.java) {
            analyzer.start(file)
        }
        assertEquals("Only images or PDFs allowed", ex.message)
    }

    @Test
    fun `start stores file, creates analysis, and enqueues worker`() {
        val bytes = "fake-pdf".toByteArray()
        val document = mockk<DocumentFile>(relaxed = true)

        val file = mockk<MultipartFile> {
            every { contentType } returns MediaType.APPLICATION_PDF_VALUE
            every { isEmpty } returns false
            every { originalFilename } returns "menu.pdf"
            every { inputStream } returns ByteArrayInputStream(bytes)
        }
        every {
            storage.store(any(), any(), any())
        } returns document

        every { repo.create(any(), any()) } returns AnalysisId(UUID.randomUUID())
        every { worker.enqueue(any()) } just Runs

        val response = analyzer.start(file)

        assertTrue(response is AnalysisResponse.AnalysisStarted)

        verify(exactly = 1){
            storage.store(any(), any(), "menu.pdf")
            repo.create(any(), any())
            worker.enqueue(any())
        }
    }
}