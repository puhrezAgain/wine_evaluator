package com.wine_evaluator.wine_evaluator.upload

import java.util.UUID
import java.time.Instant

data class Upload(
    val id: UUID,
    val filename: String,
    val contentType: String,
    val path: String,
    val uploadedAt: Instant
)