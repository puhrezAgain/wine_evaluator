package com.wineevaluator.document.interpretation

import org.springframework.stereotype.Component

import com.wineevaluator.document.model.PriceSignal
import com.wineevaluator.common.value.UploadId

@Component
class LineInterpreter {
    fun interpret(uploadId: UploadId, input: String): PriceSignal? {
        val (identityTokens, priceTokens) = input
            .let(::tokenize)
            .let(::splitPriceTokens)

        if (priceTokens.isEmpty()) return null

        val identitySet = identityTokens
            .let(::toIdentitySet)

        return PriceSignal(
            uploadId = uploadId,
            tokens = identitySet,
            prices = priceTokens,
            rawLine = input,
        )
    }

    private fun parsePriceToken(token: String): Int? {
        val digitsOnly = token.replace(Regex("[^0-9.,]"), "")
        val split = digitsOnly.split(',', '.')

        return split.firstOrNull()?.toIntOrNull()
    }

    private fun isNumericToken(token: String): Boolean =
        token.any{ it.isDigit() } &&
        token.none{ it.isLetter() }

    private fun splitPriceTokens(tokens: List<String>): Pair<List<String>, List<Int>> {
        val priceTokens = mutableListOf<Int?>()
        val identityTokens = tokens.toMutableList()

        while (identityTokens.isNotEmpty()) {
            var last = identityTokens.last()

            if (isNumericToken(last)) {
                val price = identityTokens.removeLast()
                    .let(::parsePriceToken)

                priceTokens.add(0, price)
            } else {
                break
            }
        }
        return identityTokens to priceTokens.filterNotNull()
    }
}