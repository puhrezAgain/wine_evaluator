package com.wine_evaluator.wine_evaluator.interpretation

fun processRow(row: String): TokenizedRow {
    val tokens = row
        .let(::normalize)
        .let(::tokenize)

    val (identityTokens, priceHintTokens) = splitPriceTokens(tokens)

    return TokenizedRow(identityTokens, priceHintTokens, row)
}

private fun normalize(row: String): String =
    row
        .replace(Regex("[·…]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun tokenize(row: String): List<String> =
    Regex("""[A-Za-zÀ-ÿ0-9.,'’\-]+""")
        .findAll(row)
        .map {it.value}
        .toList()

private fun isNumericToken(token: String): Boolean =
    token.any{ it.isDigit() } &&
    token.none{ it.isLetter() }

private fun splitPriceTokens(tokens: List<String>): Pair<List<String>, List<String>> {
    val priceTokens = mutableListOf<String>()
    val identityTokens = tokens.toMutableList()

    while (identityTokens.isNotEmpty()) {
        var last = identityTokens.last()

        if (isNumericToken(last)) {
            priceTokens.add(0, identityTokens.removeLast())
        } else {
            break
        }
    }
    return identityTokens to priceTokens
}
