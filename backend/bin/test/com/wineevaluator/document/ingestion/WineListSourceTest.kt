package com.wineevaluator.document.ingestion

import com.wineevaluator.common.error.ValidationException
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class WineListSourceTest {

    @Test
    fun `detects pdf source`() {
        val source = determineWineListSource(Path.of("menu.pdf"))
        assertEquals(WineListSource.PDF_TEXT, source)
    }

    @Test
    fun `detects image source`() {
        val source = determineWineListSource(Path.of("menu.jpeg"))
        assertEquals(WineListSource.OCR, source)
    }

    @Test
    fun `throws on unsupported extension`() {
        assertThrows(ValidationException::class.java) {
            determineWineListSource(Path.of("menu.exe"))
        }
    }

    @Test
    fun `throw on missing extension`() {
        assertThrows(ValidationException::class.java) { determineWineListSource(Path.of("menu")) }
    }
}
