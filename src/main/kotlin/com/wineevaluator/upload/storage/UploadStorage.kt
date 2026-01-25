package com.wineevaluator.upload.storage

import java.io.InputStream
import com.wineevaluator.common.value.UploadId

import com.wineevaluator.document.model.DocumentFile


interface UploadStorage {
    fun store(
        input: InputStream,
        uploadId: UploadId,
        filename: String
    ): DocumentFile
}