package com.wineevaluator.wine

import com.wineevaluator.common.error.ValidationException
import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.BottlePricedSignal
import com.wineevaluator.document.persistence.JpaPriceSignalRepository
import com.wineevaluator.wine.matching.MatchStrategy
import com.wineevaluator.wine.model.NewWine
import com.wineevaluator.wine.model.WineMatch
import com.wineevaluator.wine.model.WineQueryRequest
import io.mockk.*
import java.util.UUID
import kotlin.math.roundToInt
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
                val ex =
                        assertThrows(ValidationException::class.java) {
                                handler.query(WineQueryRequest("", 10f))
                        }

                assertEquals("Wine name must not be empty", ex.message)
        }

        @Test
        fun `query throws ValidationException when price non-positive`() {
                val ex =
                        assertThrows(ValidationException::class.java) {
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
                val signal =
                        BottlePricedSignal(
                                uploadId = UploadId(UUID.randomUUID()),
                                tokens = setOf("VINA", "TONDONIA", "RESERVA", "2011"),
                                price = 35,
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
                val good =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA", "TONDONIA", "RESERVA"),
                                40,
                                "Viña Tondonía Reserva 40"
                        )

                val bad =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA"),
                                40,
                                "Viña 40"
                        )

                every { priceSignalRepostory.findCandidatesByTokens(any()) } returns
                        listOf(bad, good)

                val request = WineQueryRequest("Viña Tondonía Reserva", 42f)

                val result = handler.query(request)

                assertTrue(result.first().matchTokens.containsAll(listOf("TONDONIA", "RESERVA")))
        }

        //
        // queryByUploadId
        //

        @Test
        fun `queryByUploadId returns emptyList when no signals found`() {
                val uploadId = UploadId(UUID.randomUUID())

                every { priceSignalRepostory.findByUploadId(uploadId) } returns emptyList()

                val result = handler.queryByUploadId(uploadId)

                assertTrue(result.isEmpty())
        }

        @Test
        fun `queryByUploadId signals without candidates from other uploadIds are new wines `() {
                val uploadId = UploadId(UUID.randomUUID())

                val strong =
                        BottlePricedSignal(
                                uploadId,
                                setOf("VINA", "TONDONIA", "RESERVA"),
                                40,
                                "Viña Tondonía Reserva 40"
                        )

                val strong2 =
                        BottlePricedSignal(
                                uploadId,
                                setOf("VINA", "TONDONIA", "CRIANZA"),
                                40,
                                "Viña Tondonía Crianza 40"
                        )

                every { priceSignalRepostory.findByUploadId(uploadId) } returns
                        listOf(strong, strong2)
                every { priceSignalRepostory.findCandidatesByTokens(any()) } returns
                        listOf(strong, strong2)

                val result = handler.queryByUploadId(uploadId)
                assertFalse(result.isEmpty())
                assertTrue(result.first() is NewWine)
        }

        @Test
        fun `queryByUploadId considers signals with only weak candidates new matches`() {
                val uploadId = UploadId(UUID.randomUUID())

                val strong =
                        BottlePricedSignal(
                                uploadId,
                                setOf("VINA", "TONDONIA", "RESERVA"),
                                40,
                                "Viña Tondonía Reserva 40"
                        )

                val weak =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA"),
                                40,
                                "Viña 40"
                        )

                every { priceSignalRepostory.findByUploadId(uploadId) } returns listOf(strong)
                every { priceSignalRepostory.findCandidatesByTokens(any()) } returns
                        listOf(strong, weak)

                val result = handler.queryByUploadId(uploadId)

                assertFalse(result.isEmpty())
                assertTrue(result.first() is NewWine)
        }

        @Test
        fun `queryByUploadId with AVERAGE strategy returns one match per signal averaging strong signals`() {
                val uploadId = UploadId(UUID.randomUUID())

                val signal =
                        BottlePricedSignal(
                                uploadId,
                                setOf("VINA", "TONDONIA", "RESERVA"),
                                40,
                                "Viña Tondonía Reserva 40"
                        )

                val strong =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA", "TONDONIA", "RESERVA"),
                                45,
                                "Viña Tondonía Reserva 40"
                        )

                val strong2 =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA", "TONDONIA", "RESERVA", "2011"),
                                50,
                                "Viña Tondonía Reserva 2011 50"
                        )

                val weak =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA"),
                                40,
                                "Viña 40"
                        )

                every { priceSignalRepostory.findByUploadId(uploadId) } returns listOf(signal)
                every { priceSignalRepostory.findCandidatesByTokens(any()) } returns
                        listOf(weak, signal, strong, strong2)

                val result = handler.queryByUploadId(uploadId, MatchStrategy.AVERAGE)

                assertEquals(1, result.size)

                val match = result.first() as WineMatch
                assertEquals(listOf(45, 50).average().roundToInt(), match.referencePrice)
        }

        @Test
        fun `queryByUploadId with BEST strategy returns best match per signal`() {
                val uploadId = UploadId(UUID.randomUUID())

                val signal =
                        BottlePricedSignal(
                                uploadId,
                                setOf("VINA", "TONDONIA", "RESERVA", "2011"),
                                40,
                                "Viña Tondonía Reserva 40"
                        )

                val strong =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA", "TONDONIA", "RESERVA"),
                                45,
                                "Viña Tondonía Reserva 40"
                        )

                val strongest =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA", "TONDONIA", "RESERVA", "2011"),
                                50,
                                "Viña Tondonía Reserva 2011 50"
                        )

                val weak =
                        BottlePricedSignal(
                                UploadId(UUID.randomUUID()),
                                setOf("VINA"),
                                40,
                                "Viña 40"
                        )

                every { priceSignalRepostory.findByUploadId(uploadId) } returns listOf(signal)
                every { priceSignalRepostory.findCandidatesByTokens(any()) } returns
                        listOf(weak, signal, strong, strongest)

                val result = handler.queryByUploadId(uploadId, MatchStrategy.BEST)

                assertEquals(1, result.size)

                val match = result.first() as WineMatch
                assertEquals(strongest.price, match.referencePrice)
        }
}
