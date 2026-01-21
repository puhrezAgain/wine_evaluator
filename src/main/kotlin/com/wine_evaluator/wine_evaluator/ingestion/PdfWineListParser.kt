package com.wine_evaluator.wine_evaluator.ingestion

import java.nio.file.Path
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper


fun parsePDF(pdfPath: Path): List<String> {
    PDDocument.load(pdfPath.toFile()).use { document ->
        val stripper = PDFTextStripper().apply {
            sortByPosition = true
        }

        val text = stripper.getText(document)
            .lines()
            .mapNotNull { it.trim().takeIf { it.isNotEmpty() }}

        return text
    }
}

