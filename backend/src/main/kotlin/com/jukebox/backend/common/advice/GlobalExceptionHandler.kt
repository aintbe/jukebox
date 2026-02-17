package com.jukebox.backend.common.advice

import com.jukebox.backend.common.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(e.statusCode)
            .body(GlobalResponse.error(e.errorCode, e.reason ?: ""))

    // Handle errors thrown by [require*] or [check*] assertions.
    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleAssertException(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.error("Assertion Failed:", e)
        return handleBusinessException(InternalException())
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleUnknownException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .badRequest()
            .body(GlobalResponse.error("CANNOT_PARSE_REQUEST_BODY", e.message ?: ""))
}

private class InternalException :
    BusinessException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_SERVER_ERROR",
        "Server encountered an unexpected error.",
    )
