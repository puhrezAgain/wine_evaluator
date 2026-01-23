package com.wineevaluator.upload.storage

import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.UUID

import com.wineevaluator.upload.UploadedFile


interface UploadStorage {
    fun store(
        file: MultipartFile
    ): UploadedFile
}