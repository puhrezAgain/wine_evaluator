package com.wineevaluator.upload

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/uploads")
class UploadController(
    private val uploadHandler: UploadHandler
){

    @PostMapping(
        consumes = arrayOf(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.APPLICATION_PDF_VALUE
        )
    )
    fun upload(
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<UploadedFile> {
        if (file.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Empty file"
            )
        }

        if (file.contentType.isNullOrBlank()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Missing Content-Type"
            )
        }

        val upload = uploadHandler.handleUpload(file)
        return ResponseEntity.status(HttpStatus.CREATED).body(upload)
    }
}