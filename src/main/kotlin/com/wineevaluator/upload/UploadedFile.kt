package com.wineevaluator.upload

import java.util.UUID
import java.time.Instant
import java.nio.file.Path
import com.wineevaluator.common.value.UploadId

data class UploadedFile(
    val id: UploadId,
    val filename: String,
    val path: Path,
    val uploadedAt: Instant
)