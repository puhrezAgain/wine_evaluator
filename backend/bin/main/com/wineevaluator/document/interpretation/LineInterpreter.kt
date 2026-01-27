package com.wineevaluator.document.interpretation

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.PriceSignal
import java.time.Year
import org.springframework.stereotype.Component

private const val MIN_ALLOWED_YEAR = 1950
private val MAX_ALLOWED_YEAR = Year.now().value

@Component
class LineInterpreter {
    fun interpret(
            uploadId: UploadId,
            input: String,
    ): PriceSignal? {
        val (identityTokens, priceTokens) = input.let(::tokenize).let(::splitPriceTokens)

        if (priceTokens.isEmpty()) return null

        val identitySet = identityTokens.let(::toIdentityTokens)

        return PriceSignal(
                uploadId = uploadId,
                tokens = identitySet,
                prices = priceTokens,
                rawLine = input,
        )
    }

    private fun parsePriceToken(token: String): Int? =
            token.replace(Regex("[^0-9.,]"), "").split(',', '.').firstOrNull()?.toIntOrNull()

    private fun isYearToken(token: String): Boolean =
            token.toIntOrNull()?.let { it in MIN_ALLOWED_YEAR..MAX_ALLOWED_YEAR } == true

    private fun isNumericToken(token: String): Boolean =
            token.any { it.isDigit() } && token.none { it.isLetter() }

    private fun splitPriceTokens(tokens: List<String>): Pair<List<String>, List<Int>> {
        val priceTokens = mutableListOf<Int?>()
        val identityTokens = tokens.toMutableList()

        while (identityTokens.lastOrNull()?.let(::isNumericToken) == true &&
                identityTokens.lastOrNull()?.let(::isYearToken) == false) {
            priceTokens.add(
                    0,
                    identityTokens.removeLast().let(::parsePriceToken),
            )
        }

        return identityTokens to priceTokens.filterNotNull().filter { it > 0 }
    }
}
