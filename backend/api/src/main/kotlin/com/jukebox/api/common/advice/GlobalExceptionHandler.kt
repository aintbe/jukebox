package com.jukebox.api.common.advice

import com.jukebox.core.dto.ErrorResponse
import com.jukebox.core.dto.GlobalResponse
import com.jukebox.core.exception.BindingException
import com.jukebox.core.exception.BusinessException
import com.jukebox.core.exception.InternalException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(e.statusCode)
            .body(GlobalResponse.error(e.errorCode, e.reason))

    // Handle errors thrown by [require*] or [check*] assertions.
    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleAssertException(e: RuntimeException): ResponseEntity<ErrorResponse> {
        log.error("Assertion Failed:", e)
        return handleBusinessException(InternalException())
    }

    @ExceptionHandler(ServletRequestBindingException::class)
    fun handleBindingException(e: ServletRequestBindingException): ResponseEntity<GlobalResponse<Nothing>> =
        handleBusinessException(BindingException(e.message ?: "Failed to bind request to controller."))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(GlobalResponse.error("CANNOT_READ_BODY", e.message ?: ""))

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFoundException(e: NoResourceFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(GlobalResponse.error("NO_RESOURCE", e.message ?: ""))

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(GlobalResponse.error("METHOD_NOT_ALLOWED", e.message ?: ""))
}
