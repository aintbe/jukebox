package com.jukebox.relay.common.web

import com.jukebox.core.constants.HttpConstants
import com.jukebox.core.dto.RequestContext
import com.jukebox.core.dto.StreamingAccess
import com.jukebox.core.exception.BadRequestException
import kotlinx.coroutines.reactor.mono
import org.springframework.core.MethodParameter
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.time.Instant

class RequestContextResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentRequestContext::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange,
    ): Mono<Any> {
        val request = exchange.request

        return mono {
            val userId = request.getHeader<Long>(HttpConstants.RELAY_USER_ID)
            val jukeboxId = request.getHeader<Long>(HttpConstants.RELAY_JUKEBOX_ID)
            val serviceName = request.getHeader<String>(HttpConstants.RELAY_STREAMING_SERVICE)
            val token = request.getHeader<String>(HttpConstants.RELAY_STREAMING_TOKEN)
            val expiresAt = request.getHeader<Instant?>(HttpConstants.RELAY_STREAMING_EXPIRES_AT)

            RequestContext(
                jukeboxId,
                userId,
                StreamingAccess(
                    serviceName,
                    token,
                    expiresAt,
                ),
            )
        }
    }

    private inline fun <reified T> ServerHttpRequest.getHeader(name: String): T {
        val value = this.headers.getFirst(name)
        if (value == null) {
            if (null is T) return null as T // T is nullable
            throw BadRequestException("HEADER_MISSING", "$name is required in request headers.")
        }

        val converted =
            when (T::class) {
                String::class -> value
                Long::class -> value.toLongOrNull()
                Instant::class -> runCatching { Instant.parse(value) }.getOrNull()
                else -> throw IllegalArgumentException("Unsupported type: ${T::class.simpleName}")
            }

        return converted as? T
            ?: throw BadRequestException("INVALID_HEADER", "Request header $name must be ${T::class.simpleName}.")
    }
}
