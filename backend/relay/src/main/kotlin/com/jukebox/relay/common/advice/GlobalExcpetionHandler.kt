package com.jukebox.relay.common.advice

import com.jukebox.core.dto.BusinessExcpetionDto
import com.jukebox.core.exception.BusinessException
import com.jukebox.core.exception.InternalException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.resource.NoResourceFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    suspend fun handleBusinessException(e: BusinessException): ResponseEntity<BusinessExcpetionDto> =
        ResponseEntity
            .status(e.statusCode)
            .body(BusinessExcpetionDto.from(e))

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    suspend fun handleAssertException(e: RuntimeException): ResponseEntity<BusinessExcpetionDto> {
        log.error("Assertion Failed: ${e.message}", e)
        return handleBusinessException(InternalException())
    }

    @ExceptionHandler(NoResourceFoundException::class)
    suspend fun handleNotFoundException(e: NoResourceFoundException): ResponseEntity<BusinessExcpetionDto> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                BusinessExcpetionDto(
                    statusValue = HttpStatus.NOT_FOUND.value(),
                    errorCode = "NOT_FOUND",
                    reason = e.message,
                ),
            )
}
