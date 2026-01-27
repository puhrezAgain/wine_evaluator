package com.wineevaluator.upload.storage

import com.wineevaluator.common.value.UploadId
import com.wineevaluator.document.model.DocumentFile
import java.io.InputStream

interface UploadStorage {
    fun store(
            input: InputStream,
            uploadId: UploadId,
            filename: String,
    ): DocumentFile
}
