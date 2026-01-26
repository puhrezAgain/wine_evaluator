package com.wineevaluator.wine

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.wineevaluator.document.persistence.JpaPriceSignalRepository
import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.common.value.UploadId
import java.util.UUID

class WineQueryHandlerTest {
    private val priceSignalRepostory = mockk<JpaPriceSignalRepository>(relaxed = true)

    private lateinit var handler: WineQueryHandler

    @BeforeEach
    fun setUp() {
        handler = WineQueryHandler(priceSignalRepostory)
    }

    @Test
    fun `query returns empty list when no prices found`() {
        every { priceSignalRepostory.findCandidatesByTokens(any()) } returns emptyList()

        val result = handler.query("Viña Tonodía Reserva 2011", 48)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `query calculate match with correct delta and percent`() {
        val signal = PriceSignal(
            uploadId = UploadId(UUID.randomUUID()),
            tokens = setOf("VINA", "TONDONIA", "RESERVA", "2011"),
            prices = listOf(35),
            rawLine = "Viña Tondonía Reserva 2011 35"
        )

        every { priceSignalRepostory.findCandidatesByTokens(any()) } returns listOf(signal)

        val result = handler.query("Viña Tondonía Reserva 2011", 48)

        assertEquals(1, result.size)

        val match = result.first()

        assertEquals(48, match.price)
        assertEquals(35, match.referencePrice)
        assertEquals(13, match.delta)
        assertTrue(match.deltaPercent > 0.3)
    }

    @Test
    fun `query sortds matches by descending score`() {
        val good = PriceSignal(
            UploadId(UUID.randomUUID()),
            setOf("VINA", "TONDONIA", "RESERVA"),
            listOf(40),
            "Viña Tondonía Reserva 40"
        )

        val bad = PriceSignal(
            UploadId(UUID.randomUUID()),
            setOf("VINA"),
            listOf(40),
            "Viña 40"
        )

        every { priceSignalRepostory.findCandidatesByTokens(any()) } returns listOf(bad, good)

        val result = handler.query("Viña Tondonía Reserva", 42)

        assertTrue(result.first().matchTokens.containsAll(listOf("TONDONIA", "RESERVA")))
    }
}