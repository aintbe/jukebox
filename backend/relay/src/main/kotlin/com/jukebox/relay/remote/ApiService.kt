package com.jukebox.relay.remote

import com.jukebox.core.dto.GlobalResponse
import com.jukebox.core.dto.SharedDto
import com.jukebox.core.dto.StreamingAccess
import com.jukebox.core.properties.EndpointProperties
import com.jukebox.relay.common.cache.CacheService
import com.jukebox.relay.jukebox.MessagePublisher
import com.jukebox.relay.remote.web.RemoteClientService
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import reactor.core.publisher.Mono

@Service
class ApiService(
    endpointProperties: EndpointProperties,
    private val cacheService: CacheService,
    private val messagePublisher: MessagePublisher,
) : RemoteClientService(config = endpointProperties.api) {
    override fun extractErrorMessage(response: ClientResponse): Mono<String> =
        response
            .bodyToMono(GlobalResponse::class.java)
            .map { it.message ?: it.error ?: "" }

    suspend fun reissueStreamingAccess(
        serviceName: String,
        hostId: Long,
    ): StreamingAccess? {
        try {
            return client
                .request(
                    method = HttpMethod.POST,
                    uri = "/relay/reissue",
                    body = SharedDto.ReissueAccessRequest(serviceName, hostId),
                ).awaitBodyOrNull<GlobalResponse<StreamingAccess>>()
                ?.data
        } catch (e: Exception) {
            log.error("Error reissuing access", e)
            // TODO: check the error code
            //     eventPublisher.
            //     throw StreamingServiceAuthRequiredException(userId)
        }
        return null
    }
}
