package com.wineevaluator.document
import org.springframework.stereotype.Component
import com.wineevaluator.document.ingestion.DocumentParser
import com.wineevaluator.document.interpretation.LineInterpreter
import com.wineevaluator.document.persistence.PriceSignalWriter
import com.wineevaluator.upload.UploadedFile

@Component
class DocumentProcessingPipeline(
    private val parser: DocumentParser,
    private val interpreter: LineInterpreter,
    private val writer: PriceSignalWriter
){
    fun process(file: UploadedFile) {
        val lines = parser.parse(file)

        val signals = lines
            .map{ interpreter.interpret(file.id, it) }
            .filterNotNull()

        writer.write(signals)
    }
}