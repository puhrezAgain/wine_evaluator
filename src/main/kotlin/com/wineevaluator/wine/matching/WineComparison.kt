package com.wineevaluator.wine.matching


fun hybridScore(query: Set<String>, stored :Set<String>): Double {
    if (query.isEmpty()) return 0.0
    val intersection = query intersect stored

    if (intersection.size < 2) return 0.0

    val containment =
        intersection.size.toDouble() / query.size

    val jaccard =
        intersection.size.toDouble() /
        (query.size + stored.size - intersection.size)

    return 0.7 * containment + 0.3 * jaccard
}
