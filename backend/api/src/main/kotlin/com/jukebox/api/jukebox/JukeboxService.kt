package com.jukebox.api.jukebox

import com.jukebox.core.dto.RequestContext
import com.jukebox.core.dto.RequestDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service

@Service
class JukeboxService(
    private val relayClient: RelayClient,
) {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    fun connect(
        context: RequestContext,
        request: RequestDto.Connect,
    ) {
        relayClient.request<String>(
            method = HttpMethod.PUT,
            uri = "connect",
            context = context,
            body = request,
        )
    }
}
