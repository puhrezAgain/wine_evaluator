package com.wineevaluator.wine

import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.wineevaluator.document.persistence.JpaPriceSignalRepository
import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.common.value.UploadId
import com.wineevaluator.common.error.ValidationException
import com.wineevaluator.wine.model.WineQueryRequest

import java.util.UUID
import kotlin.math.roundToInt

class WineQueryHandlerTest {
    private val priceSignalRepostory = mockk<JpaPriceSignalRepository>(relaxed = true)

    private lateinit var handler: WineQueryHandler

    @BeforeEach
    fun setUp() {
        handler = WineQueryHandler(priceSignalRepostory)
    }

    //
    // query
    //

    @Test
    fun `query throws ValidationException when wine empty`() {
        val ex = assertThrows(ValidationException::class.java) {
            handler.query(WineQueryRequest("", 10f))
        }

        assertEquals("Wine name must not be empty", ex.message)

    }

    @Test
    fun `query throws ValidationException when price non-positive`() {
        val ex = assertThrows(ValidationException::class.java) {
            handler.query(WineQueryRequest("Viña Tondonia", 0f))
        }

        assertEquals("Price must be positive", ex.message)
    }


    @Test
    fun `query returns empty list when no prices found`() {
        every { priceSignalRepostory.findCandidatesByTokens(any()) } returns emptyList()
        val request = WineQueryRequest("Viña Tonodía Reserva 2011", 48f)
        val result = handler.query(request)

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
        val request = WineQueryRequest("Viña Tonodía Reserva 2011", 48f)

        val result = handler.query(request)

        assertEquals(1, result.size)

        val match = result.first()

        assertEquals(48, match.price)
        assertEquals(35, match.referencePrice)
        assertEquals(13, match.delta)
        assertTrue(match.deltaPercent > 0.3)
    }

    @Test
    fun `query sorts matches by descending score`() {
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

        val request = WineQueryRequest("Viña Tondonía Reserva", 42f)

        val result = handler.query(request)

        assertTrue(result.first().matchTokens.containsAll(listOf("TONDONIA", "RESERVA")))
    }

    //
    // queryByUploadId
    //

    @Test
    fun `queryByUploadId returns emptyList when no singals found`() {
        val uploadId = UploadId(UUID.randomUUID())

        every { priceSignalRepostory.findByUploadId(uploadId) } returns emptyList()

        val result = handler.queryByUploadId(uploadId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `queryByUploadId doesnt match candidates from the same uploadId`() {
        val uploadId = UploadId(UUID.randomUUID())

        val strong = PriceSignal(
            uploadId,
            setOf("VINA", "TONDONIA", "RESERVA"),
            listOf(40),
            "Viña Tondonía Reserva 40"
        )

        val strong2 = PriceSignal(
            uploadId,
            setOf("VINA", "TONDONIA", "CRIANZA"),
            listOf(40),
            "Viña Tondonía Crianza 40"
        )

        every { priceSignalRepostory.findByUploadId(uploadId) } returns listOf(strong, strong2)
        every { priceSignalRepostory.findCandidatesByTokens(any()) } returns listOf(strong, strong2)

        val result = handler.queryByUploadId(uploadId)

        assertEquals(0, result.size)
    }


    @Test
    fun `queryByUploadId doesnt match weak candidates`() {
        val uploadId = UploadId(UUID.randomUUID())

        val strong = PriceSignal(
            uploadId,
            setOf("VINA", "TONDONIA", "RESERVA"),
            listOf(40),
            "Viña Tondonía Reserva 40"
        )
        val weak = PriceSignal(
            UploadId(UUID.randomUUID()),
            setOf("VINA"),
            listOf(40),
            "Viña 40"
        )
        every { priceSignalRepostory.findByUploadId(uploadId) } returns listOf(strong)
        every { priceSignalRepostory.findCandidatesByTokens(any()) } returns listOf(strong, weak)

        val result = handler.queryByUploadId(uploadId)

        assertEquals(0, result.size)
    }

    @Test
    fun `queryByUploadId returns one match per signal averaging strong signals`() {
        val uploadId = UploadId(UUID.randomUUID())

        val signal = PriceSignal(
            uploadId,
            setOf("VINA", "TONDONIA", "RESERVA"),
            listOf(40),
            "Viña Tondonía Reserva 40"
        )

        val strong = PriceSignal(
            UploadId(UUID.randomUUID()),
            setOf("VINA", "TONDONIA", "RESERVA"),
            listOf(45),
            "Viña Tondonía Reserva 40"
        )

        val strong2 = PriceSignal(
            UploadId(UUID.randomUUID()),
            setOf("VINA", "TONDONIA", "RESERVA", "2011"),
            listOf(50),
            "Viña Tondonía Reserva 2011 50"
        )

        val weak = PriceSignal(
            UploadId(UUID.randomUUID()),
            setOf("VINA"),
            listOf(40),
            "Viña 40"
        )

        every { priceSignalRepostory.findByUploadId(uploadId) } returns listOf(signal)
        every { priceSignalRepostory.findCandidatesByTokens(any()) } returns listOf(weak, signal, strong, strong2)

        val result = handler.queryByUploadId(uploadId)

        assertEquals(1, result.size)
        assertEquals(listOf(45, 50).average().roundToInt(), result.first().referencePrice)
    }
    @Test
    fun `queryByUploadId ignores signals without prices`() {
        val uploadId = UploadId(UUID.randomUUID())

        val invalid = PriceSignal(
            uploadId,
            setOf("VINA", "TONDONIA"),
            emptyList(),
            "Viña Tondonía"
        )

        every { priceSignalRepostory.findByUploadId(uploadId) } returns listOf(invalid)

        val result = handler.queryByUploadId(uploadId)

        assertTrue(result.isEmpty())
    }
}