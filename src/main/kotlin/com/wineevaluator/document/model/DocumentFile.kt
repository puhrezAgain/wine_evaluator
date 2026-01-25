package com.wineevaluator.document.model

import com.wineevaluator.common.value.UploadId
import java.nio.file.Path
import java.time.Instant
import java.util.UUID

data class DocumentFile(
    val id: UploadId,
    val filename: String,
    val path: Path,
    val uploadedAt: Instant,
)
