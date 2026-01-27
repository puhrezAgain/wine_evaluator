package com.wineevaluator.analysis

import com.wineevaluator.analysis.queue.AsyncAnalysisWorker
import com.wineevaluator.analysis.persistence.JpaAnalysisRepository
import com.wineevaluator.upload.storage.LocalStorage
import com.wineevaluator.common.error.ValidationException
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.model.AnalysisRecord

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.MediaType
import java.io.ByteArrayInputStream
import java.time.Instant
import java.nio.file.Path
import java.util.UUID


class StartAnalysisPipelineTest {

    private val queue = mockk<AsyncAnalysisWorker>(relaxed = true)
    private val repo = mockk<JpaAnalysisRepository>(relaxed = true)
    private val storage = mockk<LocalStorage>()

    private lateinit var pipeline: StartAnalysisPipeline

    @BeforeEach
    fun setUp() {
        pipeline = StartAnalysisPipeline(
            queue = queue,
            repo = repo,
            storage = storage
        )
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
            pipeline.execute(file)
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
            pipeline.execute(file)
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
        every { queue.enqueue(any()) } just Runs

        pipeline.execute(file)

        verify(exactly = 1){
            storage.store(any(), any(), "menu.pdf")
            repo.create(any(), any())
            queue.enqueue(any())
        }
    }
}