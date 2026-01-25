package com.wineevaluator.document
import org.springframework.stereotype.Component
import com.wineevaluator.document.ingestion.DocumentParser
import com.wineevaluator.document.interpretation.LineInterpreter
import com.wineevaluator.document.persistence.PriceSignalRepository
import com.wineevaluator.document.model.DocumentFile

@Component
class DocumentProcessingPipeline(
    private val parser: DocumentParser,
    private val interpreter: LineInterpreter,
    private val repo: PriceSignalRepository
){
    fun process(file: DocumentFile) {
        val lines = parser.parse(file)

        val signals = lines
            .map{ interpreter.interpret(file.id, it) }
            .filterNotNull()

        repo.write(signals)
    }
}