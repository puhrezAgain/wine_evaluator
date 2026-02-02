package com.wineevaluator.common.http

import com.wineevaluator.common.error.NotFoundException
import com.wineevaluator.common.error.ProcessingException
import com.wineevaluator.common.error.ValidationException
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

class ApiExceptionHandlerTest {
    private val handler = ApiExceptionHandler()

    @Test
    fun `validation exception maps to 400`() {
        val response = handler.handleValidation(ValidationException("Invalid input"))

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val body = response.body!!
        assertEquals("VALIDATION_ERROR", body.code)
        assertTrue(body.message!!.contains("Invalid input"))
    }

    @Test
    fun `not found exception maps to 404`() {
        val response = handler.handleNotFound(NotFoundException("Analysis not found"))

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `processing exception maps to 422`() {
        val response = handler.handleDomain(ProcessingException("Domain error occurred"))

        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.statusCode)
    }

    @Test
    fun `type mismatch exception maps to 400`() {
        val response =
                handler.handleTypeMismatch(
                        mockk<MethodArgumentTypeMismatchException>(relaxed = true)
                )

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}
