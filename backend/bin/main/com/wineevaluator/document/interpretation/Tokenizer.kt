package com.wineevaluator.document.interpretation

import com.wineevaluator.common.value.UploadId
import java.text.Normalizer

private const val MIN_IDENTITY_TOKEN_LEN = 3

fun tokenizeLine(
        uploadId: UploadId,
        input: String,
): TokenizedLine =
        TokenizedLine(
                uploadId = uploadId,
                tokens = tokenize(input),
                raw = input,
        )

fun tokenize(input: String): List<String> =
        Regex("""[A-Za-zÀ-ÿ0-9]+""").findAll(input).map { it.value }.map(::normalize).toList()

private fun normalize(row: String): String =
        row.replace(Regex("[·…]+"), " ").replace(Regex("\\s+"), " ").trim()

fun toIdentityTokens(input: List<String>): Set<String> =
        input.map(::normalizeIdentity).filter { it.length >= MIN_IDENTITY_TOKEN_LEN }.toSet()

private fun normalizeIdentity(token: String): String =
        Normalizer.normalize(token, Normalizer.Form.NFD)
                .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                .uppercase()
                .replace(Regex("[’'`´]"), "")
                .replace(Regex("[^A-Z0-9]"), "")
