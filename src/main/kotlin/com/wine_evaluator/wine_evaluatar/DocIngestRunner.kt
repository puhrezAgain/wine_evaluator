package com.wine_evaluator.wine_evaluator

import java.nio.file.Paths
import com.wine_evaluator.wine_evaluator.ocr.parseImage
import com.wine_evaluator.wine_evaluator.ingestion.detectSource
import com.wine_evaluator.wine_evaluator.ingestion.WineListSource
import com.wine_evaluator.wine_evaluator.ingestion.parsePDF
import com.wine_evaluator.wine_evaluator.interpretation.processRow


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: ocr <path-to-image>")
        return
    }

    val path = Paths.get(args[0])

    println("Running on: $path")

    // receieve a file via http
    // store raw data to local or cloud
    // persist upload request
    // queue parse request

    // worker then does
    val lines = when (detectSource(path)) {
        WineListSource.OCR ->
            parseImage(path)
        WineListSource.PDF_TEXT ->
            parsePDF(path)
    }
    // persist raw lines all together associated with each upload
    val tokenizedRows =
        lines
            .mapNotNull { processRow(it) }

    // store each identityToken associated with the upload
    // store each priceToken associated with each identity

    tokenizedRows.forEach(::println)
}
