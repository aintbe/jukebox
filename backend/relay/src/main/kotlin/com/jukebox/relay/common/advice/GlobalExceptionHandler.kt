package com.jukebox.relay.common.advice

import com.jukebox.core.dto.BusinessExceptionDto
import com.jukebox.core.exception.BusinessException
import com.jukebox.core.exception.InternalException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.resource.NoResourceFoundException

/**
 * This application should convert all possible exceptions into [BusinessExceptionDto]
 * so that API server can process errors in a uniform way. Refer to
 * [com.jukebox.api.jukebox.RelayClient] for implementation.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    suspend fun handleBusinessException(e: BusinessException): ResponseEntity<BusinessExceptionDto> =
        ResponseEntity
            .status(e.statusCode)
            .body(BusinessExceptionDto.from(e))

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    suspend fun handleAssertException(e: RuntimeException): ResponseEntity<BusinessExceptionDto> {
        log.error("Assertion Failed: ${e.message}", e)
        return handleBusinessException(InternalException())
    }

    @ExceptionHandler(NoResourceFoundException::class)
    suspend fun handleNotFoundException(e: NoResourceFoundException): ResponseEntity<BusinessExceptionDto> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                BusinessExceptionDto(
                    statusValue = HttpStatus.NOT_FOUND.value(),
                    errorCode = "NOT_FOUND",
                    reason = e.message,
                ),
            )

    @ExceptionHandler(WebClientResponseException::class)
    suspend fun handleWebClientException(e: WebClientResponseException): ResponseEntity<BusinessExceptionDto> {
        log.error("Unhandled external API error [${e.statusCode}]: ${e.responseBodyAsString}", e)
        return handleBusinessException(InternalException())
    }
}
