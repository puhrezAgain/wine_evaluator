package com.wineevaluator.document
import com.wineevaluator.document.ingestion.DocumentParser
import com.wineevaluator.document.interpretation.LineInterpreter
import com.wineevaluator.document.model.DocumentFile
import com.wineevaluator.document.persistence.PriceSignalRepository
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(DocumentProcessingPipeline::class.java)

@Component
class DocumentProcessingPipeline(
    private val parser: DocumentParser,
    private val interpreter: LineInterpreter,
    private val repo: PriceSignalRepository,
) {
    fun process(file: DocumentFile) {
        val lines = parser.parse(file)

        if (lines.isEmpty()) {
            log.debug("document.no.lines uploadId={}", file.id.value)
            return
        }

        log.debug("document.parsed lines={} uploadId={}", lines.size, file.id.value)

        val signals =
            lines
                .map { interpreter.interpret(file.id, it) }
                .filterNotNull()

        if (signals.isEmpty()) {
            log.debug("document.no.signals uploadId={}", file.id.value)
            return
        }

        log.debug("document.interpreted signals={} uploadId={}", signals.size, file.id.value)

        repo.write(signals)
    }
}
