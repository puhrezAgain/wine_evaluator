package com.wine_evaluator.wine_evaluator.upload

import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.UUID


interface Storage {
    fun store(
        uploadId: UUID,
        file: MultipartFile
    ): Path
}