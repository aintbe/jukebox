package com.jukebox.relay.remote.web

import com.jukebox.core.constants.HttpConstants
import com.jukebox.core.exception.BusinessException
import com.jukebox.core.exception.InternalException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

class RemoteClient(
    val remoteServer: String,
    baseUrl: String,
    maxIdleTime: Long? = null,
    val errorMessageExtractor: (ClientResponse) -> Mono<String>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val webClient: WebClient =
        run {
            // End connection and release connection pool before the connected service does.
            // WebClient will try to hold the connection indefinitely without this setting.
            val connectionProvider =
                ConnectionProvider
                    .builder("RemoteClientConnectionProvider")
                    .apply {
                        maxIdleTime?.let {
                            this.maxIdleTime(Duration.ofMinutes(it))
                            this.evictInBackground(Duration.ofMinutes(it))
                        }
                    }.lifo()
                    .build()
            val httpClient = HttpClient.create(connectionProvider)

            WebClient
                .builder()
                .baseUrl(baseUrl)
                .clientConnector(ReactorClientHttpConnector(httpClient))
                .defaultStatusHandler({ it.isSameCodeAs(HttpStatus.UNAUTHORIZED) }) {
                    handleUnauthenticated(it)
                }.defaultStatusHandler({ it.isSameCodeAs(HttpStatus.FORBIDDEN) }) {
                    handleForbidden(it)
                }.defaultStatusHandler({ it.isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS) }) {
                    handleRateLimit(it)
                }.build()
        }

    fun handleUnauthenticated(response: ClientResponse): Mono<out Throwable> =
        errorMessageExtractor(response)
            .flatMap { message ->
                log.error("$remoteServer token seems to be expired: $message")
                Mono.error(InternalException())
            }

    fun handleForbidden(response: ClientResponse): Mono<out Throwable> {
        // TODO: implement premium thing
        return Mono.error(
            BusinessException(
                HttpStatus.UPGRADE_REQUIRED,
                "${remoteServer.uppercase()}_UPGRADE_REQUIRED",
                "",
            ),
        )
    }

    fun handleRateLimit(response: ClientResponse): Mono<out Throwable> {
        // TODO: implement rate limiting using redis
        return Mono.error(
            BusinessException(
                HttpStatus.TOO_MANY_REQUESTS,
                "${remoteServer.uppercase()}_RATE_LIMIT",
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
                        it.header(HttpHeaders.AUTHORIZATION, "${HttpConstants.AUTHORIZATION_TYPE} $token")
                    } ?: it
                }

        return spec.retrieve()
    }
}
