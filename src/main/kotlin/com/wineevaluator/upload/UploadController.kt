package com.wineevaluator.upload

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException

import com.wineevaluator.document.model.DocumentFile

@RestController
@RequestMapping("/uploads")
class UploadController(
    private val uploadHandler: UploadHandler
){

    @PostMapping(
        consumes = [
            MediaType.MULTIPART_FORM_DATA_VALUE
        ]
    )
    fun upload(
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<DocumentFile> {
        val type = file.contentType ?: throw ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Missing Content-Type"
        )

        // TODO, this limits us to jpegs and pdf, worth the simplicity?
        if (!(type.startsWith("image/") || type == MediaType.APPLICATION_PDF_VALUE)) {
            throw ResponseStatusException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Only images or PDFs allowed"
        )
        }

        if (file.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Empty file"
            )
        }

        val resp = uploadHandler.handleDocument(file)

        return ResponseEntity.status(HttpStatus.CREATED).body(resp)
    }
}