package com.wineevaluator.document.interpretation

import com.wineevaluator.common.value.UploadId

data class TokenizedLine(
    val uploadId: UploadId,
    val tokens: List<String>,
    val raw: String,
)
