package com.wine_evaluator.wine_evaluator.interpretation

fun processRow(row: String): ProcessedRow? {
    val tokens = row
        .let(WineTokenizer::tokenize)

    val (identityTokens, priceHintTokens) = splitHintPriceTokens(tokens)

    if (priceHintTokens.isEmpty()) return null

    val priceTokens = priceHintTokens
        .filterNotNull()
    val identitySet = identityTokens
        .let(WineTokenizer::toIdentitySet)

    return ProcessedRow(identitySet, priceTokens, row)
}

private fun parsePriceToken(token: String): Int? {
    val digitsOnly = token.replace(Regex("[^0-9.,]"), "")
    val split = digitsOnly.split(',', '.')
    return split.firstOrNull()?.toIntOrNull()
}

private fun isNumericToken(token: String): Boolean =
    token.any{ it.isDigit() } &&
    token.none{ it.isLetter() }

private fun splitHintPriceTokens(tokens: List<String>): Pair<List<String>, List<Int?>> {
    val priceHintTokens = mutableListOf<Int?>()
    val identityTokens = tokens.toMutableList()

    while (identityTokens.isNotEmpty()) {
        var last = identityTokens.last()

        if (isNumericToken(last)) {
            val price = identityTokens.removeLast()
                .let(::parsePriceToken)

            priceHintTokens.add(0, price)
        } else {
            break
        }
    }
    return identityTokens to priceHintTokens
}
