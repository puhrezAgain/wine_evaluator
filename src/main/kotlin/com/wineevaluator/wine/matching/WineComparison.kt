package com.wineevaluator.wine.matching

private const val MIN_TOKEN_MATCHES = 2
private const val CONTAINMENT_PERCENTAGE = 0.7
private const val JACCARD_PERCENTAGE = 0.3

fun hybridScore(
    query: Set<String>,
    stored: Set<String>,
): Double {
    if (query.isEmpty()) return 0.0

    val intersection = query intersect stored

    if (intersection.size < MIN_TOKEN_MATCHES) return 0.0

    val containment =
        intersection.size.toDouble() / query.size

    val jaccard =
        intersection.size.toDouble() /
            (query.size + stored.size - intersection.size)

    return CONTAINMENT_PERCENTAGE * containment + JACCARD_PERCENTAGE * jaccard
}
