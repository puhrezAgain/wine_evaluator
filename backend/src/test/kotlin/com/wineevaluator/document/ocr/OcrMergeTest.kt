package com.wineevaluator.document.ocr

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OcrMergeTest {
    @Test
    fun `merges words on same baseline`() {
        val lines =
                listOf(
                        OcrLine("Viña", 10f, 100f, 0.9f),
                        OcrLine("Tondonía", 50f, 102f, 0.9f),
                        OcrLine("48", 200f, 101f, 0.9f),
                        OcrLine("REDS", 10f, 200f, 0.9f)
                )

        val merged = mergeLinesVisually(lines)

        assertEquals(2, merged.size)
        assertEquals("Viña Tondonía 48", merged.first())
        assertEquals("REDS", merged.last())
    }
}
