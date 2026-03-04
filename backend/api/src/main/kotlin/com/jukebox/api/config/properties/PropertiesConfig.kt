package com.jukebox.api.config.properties

import com.jukebox.core.properties.EndpointProperties
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class PropertiesConfig {
    @Bean
    fun endpointProperties(environment: Environment): EndpointProperties =
        Binder
            .get(environment)
            .bind("endpoint", EndpointProperties::class.java)
            .get()
}
