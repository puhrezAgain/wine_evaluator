package com.wine_evaluator.wine_evaluator.upload

import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus

@RestController
@RequestMapping("/uploads")
class UploadController(
    private val uploadService: UploadService
){

    @PostMapping
    fun upload(
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<Upload> {
        val upload = uploadService.handleUpload(file)
        return ResponseEntity.status(HttpStatus.CREATED).body(upload)
    }
}