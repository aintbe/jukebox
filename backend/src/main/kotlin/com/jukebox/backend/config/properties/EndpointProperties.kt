package com.jukebox.backend.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "endpoint")
data class EndpointProperties(
    val frontend: Frontend,
) {
    val oAuth2CallbackUrl = combine(frontend.domain, frontend.oAuth2Callback)

    private fun combine(vararg segments: String) =
        segments
            .joinToString("/") { it.trim().removePrefix("/").removeSuffix("/") }
}

data class Frontend(
    val domain: String,
    val oAuth2Callback: String,
)
