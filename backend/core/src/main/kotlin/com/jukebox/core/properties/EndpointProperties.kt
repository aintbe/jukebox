package com.jukebox.core.properties

data class EndpointProperties(
    val frontend: FrontendConfig,
    val api: RemoteConfig,
    val spotify: RemoteConfig,
) {
    val oAuth2SignInUrl = combine(frontend.domain, frontend.oAuth2SignIn)
    val oAuth2CallbackUrl = combine(frontend.domain, frontend.oAuth2Callback)

    private fun combine(vararg segments: String) =
        segments
            .joinToString("/") { it.trim().removePrefix("/").removeSuffix("/") }
}

data class FrontendConfig(
    val domain: String,
    val oAuth2SignIn: String,
    val oAuth2Callback: String,
)

data class RemoteConfig(
    val name: String,
    val label: String,
    val domain: String,
    val maxIdleTime: Long? = null,
)
