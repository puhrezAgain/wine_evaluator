package com.wineevaluator.document

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.ingestion.DocumentParser
import com.wineevaluator.document.interpretation.LineInterpreter
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.document.persistence.PriceSignalRepository
import io.mockk.*
import java.nio.file.Path
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DocumentProcessingPipelineTest {
    private val parser = mockk<DocumentParser>(relaxed = true)
    private val interpreter = mockk<LineInterpreter>(relaxed = true)
    private val repository = mockk<PriceSignalRepository>(relaxed = true)

    private lateinit var pipeline: DocumentProcessingPipeline

    @BeforeEach
    fun setUp() {
        pipeline = DocumentProcessingPipeline(parser, interpreter, repository)
    }

    @Test
    fun `process parses document, interprets lines, and persists signals`() {
        val document =
                DocumentFile(
                        UploadId(UUID.randomUUID()),
                        "menu.pdf",
                        Path.of("/tmp/menu.pdf"),
                        Instant.now()
                )

        val parsedLines = listOf("Viña Tondonía 2011 35")

        val signal =
                PriceSignal(
                        document.id,
                        setOf("VINA", "TONDONIA", "2011"),
                        listOf(35),
                        "Viña Tondonía 2011 35"
                )

        every { parser.parse(document) } returns parsedLines
        every { interpreter.interpret(document.id, parsedLines.first()) } returns signal

        pipeline.process(document)

        verify(exactly = 1) {
            parser.parse(document)
            interpreter.interpret(document.id, parsedLines.first())
            repository.write(listOf(signal))
        }
    }

    @Test
    fun `process does nothing when parser returns no lines`() {
        val document = mockk<DocumentFile>(relaxed = true)

        every { parser.parse(document) } returns emptyList()

        pipeline.process(document)

        verify { repository wasNot Called }
    }
}
