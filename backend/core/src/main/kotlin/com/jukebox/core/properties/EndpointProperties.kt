package com.jukebox.core.properties

data class EndpointProperties(
    val frontend: FrontendServerConfig,
    val relay: ServerConfig,
    val spotify: StreamingServerConfig,
) {
    val oAuth2CallbackUrl = combine(frontend.domain, frontend.oAuth2Callback)

    private fun combine(vararg segments: String) =
        segments
            .joinToString("/") { it.trim().removePrefix("/").removeSuffix("/") }
}

data class FrontendServerConfig(
    val domain: String,
    val oAuth2Callback: String,
)

data class ServerConfig(
    val domain: String,
)

data class StreamingServerConfig(
    val name: String,
    val label: String,
    val domain: String,
)
