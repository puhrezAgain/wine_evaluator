package com.wineevaluator.analysis

import com.wineevaluator.analysis.model.AnalysisId
import com.wineevaluator.analysis.persistence.JpaAnalysisRepository
import com.wineevaluator.analysis.queue.AsyncAnalysisWorker
import com.wineevaluator.common.error.ValidationException
import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.upload.storage.LocalStorage
import io.mockk.*
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Files
import java.time.Instant
import java.util.UUID
import kotlin.io.path.inputStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile

class StartAnalysisPipelineTest {

    private val queue = mockk<AsyncAnalysisWorker>(relaxed = true)
    private val repo = mockk<JpaAnalysisRepository>(relaxed = true)
    private val storage = mockk<LocalStorage>()

    private lateinit var pipeline: StartAnalysisPipeline

    @BeforeEach
    fun setUp() {
        pipeline = StartAnalysisPipeline(queue = queue, repo = repo, storage = storage)
    }

    //
    // start tests
    //

    @Test
    fun `start throws ValidationException when file doesn't specify content type`() {
        var file =
                mockk<MultipartFile> {
                    every { contentType } returns null
                    every { isEmpty } returns false
                }

        val ex = assertThrows(ValidationException::class.java) { pipeline.execute(file) }

        assertEquals("Missing Content-Type head", ex.message)
    }
    @Test
    fun `start throws ValidationException when file empty`() {
        var file =
                mockk<MultipartFile> {
                    every { contentType } returns "image/jpeg"
                    every { isEmpty } returns true
                }

        val ex = assertThrows(ValidationException::class.java) { pipeline.execute(file) }

        assertEquals("Empty file", ex.message)
    }

    @Test
    fun `start throws ValidationException on unsupported content type`() {
        val file =
                mockk<MultipartFile> {
                    every { contentType } returns "text/plain"
                    every { isEmpty } returns false
                }

        val ex = assertThrows(ValidationException::class.java) { pipeline.execute(file) }
        assertEquals("Only images or PDFs allowed", ex.message)
    }

    @Test
    fun `start stores nameless file with default name, creates analysis, and enqueues worker`() {
        val document = mockk<DocumentFile>(relaxed = true)

        val file =
                mockk<MultipartFile> {
                    every { contentType } returns MediaType.APPLICATION_PDF_VALUE
                    every { isEmpty } returns false
                    every { originalFilename } returns null
                    every { inputStream } returns ByteArrayInputStream("fake-pdf".toByteArray())
                }
        every { storage.store(any(), any(), any()) } returns document

        every { repo.create(any(), any()) } returns AnalysisId(UUID.randomUUID())
        every { queue.enqueue(any()) } just Runs

        pipeline.execute(file)

        verify(exactly = 1) {
            storage.store(any(), any(), "upload")
            repo.create(any(), any())
            queue.enqueue(any())
        }
    }

    @Test
    fun `executeDiagnostic processes file and returns signals`() {
        val document = mockk<DocumentFile>(relaxed = true)
        val signal = mockk<PriceSignal>(relaxed = true)
        val file =
                mockk<MultipartFile> {
                    every { contentType } returns MediaType.APPLICATION_PDF_VALUE
                    every { isEmpty } returns false
                    every { originalFilename } returns "menu.pdf"
                    every { inputStream } returns ByteArrayInputStream("fake-pdf".toByteArray())
                }
        every { storage.storeTemp(any(), any(), any()) } returns document
        every { queue.processImmediate(document) } returns listOf(signal)

        val result = pipeline.executeDiagnostic(file)

        assertTrue(result.isNotEmpty())
    }
    @Test
    fun `executeDiagnostic cleans up temp file on failure`() {
        val temp = Files.createTempFile("test", ".pdf")
        val document =
                DocumentFile(
                        path = temp,
                        id = UploadId(UUID.randomUUID()),
                        uploadedAt = Instant.now(),
                        filename = "test.pdf"
                )

        val file =
                mockk<MultipartFile> {
                    every { isEmpty } returns false
                    every { contentType } returns MediaType.APPLICATION_PDF_VALUE
                    every { originalFilename } returns document.filename
                    every { inputStream } returns ByteArrayInputStream("fake-pdf".toByteArray())
                }

        every { storage.storeTemp(any(), any(), any()) } returns document
        every { queue.processImmediate(any()) } throws RuntimeException("boom")

        assertThrows(RuntimeException::class.java) { pipeline.executeDiagnostic(file) }

        assertFalse(Files.exists(temp))
    }

    @Test
    fun `executeDiagnostic logs warning when temp file deletion fails`() {
        val temp = Files.createTempFile("test", ".pdf")

        val document =
                DocumentFile(
                        path = temp,
                        id = UploadId(UUID.randomUUID()),
                        uploadedAt = Instant.now(),
                        filename = "test.pdf"
                )

        val file =
                mockk<MultipartFile> {
                    every { isEmpty } returns false
                    every { contentType } returns MediaType.APPLICATION_PDF_VALUE
                    every { originalFilename } returns "test.pdf"
                    every { inputStream } returns Files.newInputStream(temp)
                }

        every { storage.storeTemp(any(), any(), any()) } returns document
        every { queue.processImmediate(any()) } throws RuntimeException("boom")

        mockkStatic(Files::class)
        try {
            every { Files.deleteIfExists(temp) } throws IOException("cannot delete")
            assertThrows(RuntimeException::class.java) { pipeline.executeDiagnostic(file) }
            verify { Files.deleteIfExists(temp) }
        } finally {
            unmockkStatic(Files::class)
        }
    }
}
