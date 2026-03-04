package com.jukebox.relay.config

import com.jukebox.relay.common.web.RequestContextResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer

@Configuration
class WebConfig : WebFluxConfigurer {
    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(RequestContextResolver())
    }
}
