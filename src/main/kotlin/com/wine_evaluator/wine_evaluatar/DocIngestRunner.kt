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

    val lines = when (detectSource(path)) {
        WineListSource.OCR ->
            parseImage(path)
        WineListSource.PDF_TEXT ->
            parsePDF(path)
    }

    println("====== OCR RESULT =======")
    lines
        .forEach{ println(processRow(it))}
}
