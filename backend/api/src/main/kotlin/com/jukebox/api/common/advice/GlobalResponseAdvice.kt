package com.jukebox.api.common.advice

import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice
import tools.jackson.databind.ObjectMapper

@RestControllerAdvice
class GlobalResponseAdvice(
    val objectMapper: ObjectMapper,
) : ResponseBodyAdvice<Any> {
    override fun supports(
        returnType: MethodParameter,
        converterType: Class<out HttpMessageConverter<*>>,
    ) = returnType.parameterType != GlobalResponse::class.java

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        if (body is GlobalResponse<*>) return body

        val wrappedBody = GlobalResponse.success(body)
        if (selectedContentType == MediaType.APPLICATION_JSON) return wrappedBody

        // If content type is set to [String], forcefully set the body to JSON format.
        response.headers.contentType = MediaType.APPLICATION_JSON
        return objectMapper.writeValueAsString(wrappedBody)
    }
}
