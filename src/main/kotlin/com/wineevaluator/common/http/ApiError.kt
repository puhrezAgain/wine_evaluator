package com.wineevaluator.common.http

import java.time.Instant

data class ApiError(
    val code: String,
    val message: String?,
    val timestamp: Instant = Instant.now()
)