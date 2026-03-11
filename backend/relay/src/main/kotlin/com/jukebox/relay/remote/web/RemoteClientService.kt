package com.jukebox.relay.remote.web

import com.jukebox.core.properties.RemoteConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono

abstract class RemoteClientService(
    protected val config: RemoteConfig,
) {
    protected val log: Logger = LoggerFactory.getLogger(javaClass)

    protected val remoteServerLabel = config.label
    protected val client: RemoteClient by lazy {
        RemoteClient(
            remoteServer = config.name,
            baseUrl = config.domain,
            maxIdleTime = config.maxIdleTime,
            errorMessageExtractor = ::extractErrorMessage,
        )
    }

    protected abstract fun extractErrorMessage(response: ClientResponse): Mono<String>
}
