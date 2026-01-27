package com.wineevaluator.common.http

import com.wineevaluator.common.error.DomainException
import com.wineevaluator.common.error.NotFoundException
import com.wineevaluator.common.error.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler
    fun handleValidation(ex: ValidationException): ResponseEntity<ApiError> =
            ResponseEntity.badRequest().body(ApiError("VALIDATION_ERROR", ex.message))

    @ExceptionHandler
    fun handleNotFound(ex: NotFoundException): ResponseEntity<ApiError> =
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError("NOT_FOUND", ex.message))

    @ExceptionHandler
    fun handleDomain(ex: DomainException): ResponseEntity<ApiError> =
            ResponseEntity.unprocessableContent().body(ApiError("DOMAIN_ERROR", ex.message))

    @ExceptionHandler
    fun handleException(ex: Exception): ResponseEntity<ApiError> {
        log.error("unhandled.exception", ex)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError("INTERNAL_ERROR", "Unexpected error"))
    }
}
