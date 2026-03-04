package com.jukebox.relay.common.web

import com.jukebox.core.properties.StreamingServerConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono

abstract class StreamingClientService(
    protected val serviceConfig: StreamingServerConfig,
) {
    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    val serviceLabel = serviceConfig.label
    protected val client: StreamingClient by lazy {
        StreamingClient(
            serviceName = serviceConfig.name,
            baseUrl = serviceConfig.domain,
            errorMessageExtractor = ::extractErrorMessage,
        )
    }

    protected abstract fun extractErrorMessage(response: ClientResponse): Mono<String>
}
