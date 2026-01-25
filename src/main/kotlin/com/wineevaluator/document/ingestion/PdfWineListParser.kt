package com.wineevaluator.document.ingestion

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.nio.file.Path

fun parsePDF(pdfPath: Path): List<String> {
    PDDocument.load(pdfPath.toFile()).use { document ->
        val stripper =
            PDFTextStripper().apply {
                sortByPosition = true
            }

        val text =
            stripper
                .getText(document)
                .lines()
                .mapNotNull { it.trim().takeIf { it.isNotEmpty() } }

        return text
    }
}
