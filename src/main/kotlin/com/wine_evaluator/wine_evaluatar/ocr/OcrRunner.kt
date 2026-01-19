package com.wine_evaluator.wine_evaluator.ocr

import java.nio.file.Paths

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: ocr <path-to-image>")
        return
    }

    val ocrService = GoogleVisionOcrService()

    val imagePath = Paths.get(args[0])
    
    println("Running ocr on: $imagePath")

    val lines = ocrService.extractLines(imagePath)

    println("====== OCR RESULT =======")
    lines.forEach { println(it) }
}
