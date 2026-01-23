package com.wineevaluator.document.interpretation

import com.wineevaluator.common.value.UploadId

fun tokenizeLine(uploadId: UploadId, input: String): TokenizedLine {
    return TokenizedLine(
        uploadId = uploadId,
        tokens = tokenize(input),
        raw = input
    )
}

fun tokenize(input: String): List<String> =
    Regex("""[A-Za-zÀ-ÿ0-9]+""")
        .findAll(input)
        .map { it.value }
        .map(::normalize)
        .toList()

private fun normalize(row: String): String =
    row
        .replace(Regex("[·…]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

fun toIdentitySet(input: List<String>): Set<String> =
    input
        .map(::normalizeIndentity)
        .filter { it.length >= 3 }
        .toSet()

private fun normalizeIndentity(token: String): String =
    token
        .uppercase()
        .replace(Regex("[’'`´]"), "")
        .replace(Regex("[^A-Z0-9]"), "")

