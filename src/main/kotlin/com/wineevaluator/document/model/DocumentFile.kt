package com.wineevaluator.document.model

import java.util.UUID
import java.time.Instant
import java.nio.file.Path
import com.wineevaluator.common.value.UploadId

data class DocumentFile(
    val id: UploadId,
    val filename: String,
    val path: Path,
    val uploadedAt: Instant
)