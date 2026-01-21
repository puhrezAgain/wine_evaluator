package com.wine_evaluator.wine_evaluator.upload

import org.springframework.web.multipart.MultipartFile


interface Storage {
    fun store(
        uploadId: String,
        file: MultipartFile
    ): String
}