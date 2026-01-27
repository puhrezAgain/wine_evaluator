package com.wineevaluator.document.interpretation

import com.wineevaluator.common.value.UploadId
import java.util.UUID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LineInterpreterTest {
    private val interpreter = LineInterpreter()
    private val uploadId = UploadId(UUID.randomUUID())

    @Test
    fun `ignores lines without prices`() {
        val actual = interpreter.interpret(uploadId, "Viña Tondonia Reserva")

        assertNull(actual)
    }

    @Test
    fun `extracts single price`() {
        val actual = interpreter.interpret(uploadId, "Viña Tondonia Reserva 48")
        assertNotNull(actual)
        assertEquals(listOf(48), actual!!.prices)
    }

    @Test
    fun `extracts multiple prices, excluding year`() {
        val actual = interpreter.interpret(uploadId, "Viña Tondonia Reserva 2010 45 48")

        assertNotNull(actual)
        assertEquals(listOf(45, 48), actual!!.prices)
    }

    @Test
    fun `normalizes identity tokens, including year`() {
        val actual = interpreter.interpret(uploadId, "Viña Tondonia Reserva 2010 45 48")!!
        assertTrue(actual.tokens.contains("VINA"))
        assertTrue(actual.tokens.contains("TONDONIA"))
        assertTrue(actual.tokens.contains("RESERVA"))
        assertTrue(actual.tokens.contains("2010"))
    }
}
