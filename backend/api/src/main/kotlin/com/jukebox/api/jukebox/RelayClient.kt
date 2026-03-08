package com.jukebox.api.jukebox

import com.jukebox.core.constants.HttpConstants
import com.jukebox.core.dto.BusinessExceptionDto
import com.jukebox.core.dto.RequestContext
import com.jukebox.core.exception.InternalException
import com.jukebox.core.properties.EndpointProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import tools.jackson.databind.ObjectMapper

@Component
class RelayClient(
    endpointProperties: EndpointProperties,
    objectMapper: ObjectMapper,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    val restClient: RestClient =
        RestClient
            .builder()
            .baseUrl(endpointProperties.relay.domain)
            .defaultStatusHandler({ it.isError }) { _, response ->
                throw runCatching {
                    objectMapper
                        .readValue(response.body, BusinessExceptionDto::class.java)
                        .toException()
                }.getOrElse {
                    log.error("Failed to parse body string from relay server: $it")
                    InternalException()
                }
            }.build()

    final inline fun <reified T : Any> request(
        method: HttpMethod,
        uri: String,
        context: RequestContext,
        body: Any? = null,
    ): T? {
        // Do not allow sending request body under /GET or /DELETE.
        require(!((method == HttpMethod.GET || method == HttpMethod.DELETE) && body != null)) {
            "Cannot request /$method with body"
        }

        val request =
            restClient
                .method(method)
                // `serviceName` work as controller prefix in relay server.
                .uri("/${context.streamingAccess.serviceName}/${uri.removePrefix("/")}")
                .headers { headers ->
                    headers.add(HttpConstants.RELAY_USER_ID, context.userId.toString())
                    headers.add(HttpConstants.RELAY_JUKEBOX_ID, context.jukeboxId.toString())
                    headers.add(HttpConstants.RELAY_STREAMING_SERVICE, context.streamingAccess.serviceName)
                    headers.add(HttpConstants.RELAY_STREAMING_TOKEN, context.streamingAccess.token)
                    context.streamingAccess.expiresAt?.also {
                        headers.add(HttpConstants.RELAY_STREAMING_EXPIRES_AT, it.toString())
                    }
                }.let {
                    body?.let { body -> it.body(body) } ?: it
                }
        return request.retrieve().body<T>()
    }
}
