package com.wine_evaluator.wine_evaluator.upload

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import java.time.Instant

@Service
class UploadService(
    private val storage: Storage,
    private val parseQueue: ParseQueue
) {
    fun handleUpload(file: MultipartFile): Upload {
        if (file.isEmpty()) {
            throw IllegalArgumentException("Empty file")
        }

        val contentType = file.contentType
            ?: throw IllegalArgumentException("Missing Content-Type")

        //validate
        FileValidator.detectSource(contentType)

        val uploadId = UUID.randomUUID()

        val path = storage.store(uploadId.toString(), file)

        val upload = Upload(uploadId, file.originalFilename ?: "upload", contentType, path, Instant.now())

        parseQueue.enqueue(uploadId)

        return upload
    }
}