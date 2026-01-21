package com.wine_evaluator.wine_evaluator.interpretation

fun processRow(row: String): ProcessedRow? {
    val tokens = row
        .let(::normalize)
        .let(::tokenize)

    val (identityTokens, priceHintTokens) = splitHintPriceTokens(tokens)
    val identitySet = createIdentitySet(identityTokens)

    if (priceHintTokens.isEmpty()) return null

    val priceTokens = priceHintTokens.filterNotNull()

    return ProcessedRow(identitySet, priceTokens, row)
}

private fun normalize(row: String): String =
    row
        .replace(Regex("[·…]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

private fun parsePriceToken(token: String): Int? {
    val digitsOnly = token.replace(Regex("[^0-9.,]"), "")
    val split = digitsOnly.split(',', '.')
    return split.firstOrNull()?.toIntOrNull()
}
private fun tokenize(row: String): List<String> =
    Regex("""[A-Za-zÀ-ÿ0-9.,'’\-]+""")
        .findAll(row)
        .map {it.value}
        .toList()

fun createIdentitySet(identityTokens: List<String>): Set<String> =
    identityTokens
        .map(::normalizeIdentityToken)
        .filter { it.length >= 3 }   // drop noise like “DE”, “LA”
        .toSet()

private fun normalizeIdentityToken(token: String): String =
    token
        .uppercase()
        .replace(Regex("[’'`´]"), "")
        .replace(Regex("[^A-Z0-9]"), "")

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
