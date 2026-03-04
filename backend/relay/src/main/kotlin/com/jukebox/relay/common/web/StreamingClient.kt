package com.jukebox.relay.common.web

import com.jukebox.core.exception.BusinessException
import com.jukebox.core.exception.InternalException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

class StreamingClient(
    val serviceName: String,
    baseUrl: String,
    val errorMessageExtractor: (ClientResponse) -> Mono<String>,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    private val webClient: WebClient =
        WebClient
            .builder()
            .baseUrl(baseUrl)
            .defaultStatusHandler({ it.isSameCodeAs(HttpStatus.UNAUTHORIZED) }) {
                handleUnauthenticated(it)
            }.defaultStatusHandler({ it.isSameCodeAs(HttpStatus.FORBIDDEN) }) {
                handleForbidden(it)
            }.defaultStatusHandler({ it.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS) }) {
                handleRateLimit(it)
            }.build()

    fun handleUnauthenticated(response: ClientResponse): Mono<out Throwable> =
        errorMessageExtractor(response)
            .flatMap { message ->
                log.error("$serviceName token seems to be expired: $message")
                Mono.error(InternalException())
            }

    fun handleForbidden(response: ClientResponse): Mono<out Throwable> {
        // TODO: implement premium thing
        return Mono.error(
            BusinessException(
                HttpStatus.UPGRADE_REQUIRED,
                "${serviceName.uppercase()}_UPGRADE_REQUIRED",
                "",
            ),
        )
    }

    fun handleRateLimit(response: ClientResponse): Mono<out Throwable> {
        // TODO: implement rate limiting using redis
        return Mono.error(
            BusinessException(
                HttpStatus.TOO_MANY_REQUESTS,
                "${serviceName.uppercase()}_RATE_LIMIT",
                "",
            ),
        )
    }

    fun request(
        method: HttpMethod,
        uri: String,
        token: String? = null,
        body: Any? = null,
    ): WebClient.ResponseSpec {
        require(!((method == HttpMethod.GET || method == HttpMethod.DELETE) && body != null)) {
            "Cannot request /$method with body"
        }

        val spec =
            webClient
                .method(method)
                .uri(uri)
                .let {
                    body?.let { body -> it.bodyValue(body) } ?: it
                }.let {
                    token?.let { token ->
                        it.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    } ?: it
                }

        return spec.retrieve()
    }
}
