package com.wineevaluator.wine.matching

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class WineComparisonTest {

    @Test
    fun `returns zero when query token empty`() {
        val actual = hybridScore(query = emptySet(), stored = setOf("RIOJA", "RESERVA"))

        assertEquals(0.0, actual)
    }

    @Test
    fun `returns zero when intersection below MIN`() {
        val actual = hybridScore(query = setOf("RIOJA"), stored = setOf("ROIJA", "RESERVA"))

        assertEquals(0.0, actual)
    }

    @Test
    fun `returns high score for perfect match`() {
        val tokens = setOf("VINA", "TONDONIA", "RESERVA")
        val actual = hybridScore(query = tokens, stored = tokens)

        assertTrue(actual > 0.9)
    }

    @Test
    fun `returns partial score for partial overlap`() {
        val actual =
                hybridScore(
                        query = setOf("VINA", "TODONIA", "RESERVA"),
                        stored = setOf("VINA", "TODONIA", "BLANCO")
                )

        assertTrue(actual in 0.4..0.8)
    }
}
