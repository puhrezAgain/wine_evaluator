package com.wineevaluator.document.interpretation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TokenizerTest {

    @Test
    fun `tokenizes alphanumeric words`() {
        val actual = tokenize("Viña Tondonía Reserva 2011")

        assertEquals(
            listOf("Viña", "Tondonía", "Reserva", "2011"), actual)
    }

    @Test
    fun `normalizes identity tokens`() {
        val actual = toIdentityTokens(listOf("Viña", "Tondonía", "Reserva", "2011"))

        assertTrue(actual.contains("VINA"))
        assertTrue(actual.contains("TONDONIA"))
        assertTrue(actual.contains("RESERVA"))
        assertTrue(actual.contains("2011"))
    }

    @Test
    fun `filters out short tokens`() {
        val actual = toIdentityTokens(listOf("La", "De", "Rioja"))

        assertEquals(setOf("RIOJA"), actual)
    }
}