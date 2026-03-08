package com.jukebox.relay.config

import com.jukebox.core.properties.EndpointProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig {
    @Bean
    fun corsWebFilter(endpointProperties: EndpointProperties): CorsWebFilter {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = listOf(endpointProperties.frontend.domain)
                allowedMethods = listOf("GET", "PUT", "OPTIONS")
                allowedHeaders = listOf("*")
                allowCredentials = true
            }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return CorsWebFilter(source)
    }
}
