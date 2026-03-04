package com.jukebox.relay.common.web

import org.springframework.web.reactive.config.WebFluxConfigurer

/**
 * Annotation that is used to resolve [com.jukebox.core.dto.RequestContext]
 * to a method argument. To use this in controller, register [RequestContextResolver]
 * to [WebFluxConfigurer].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentRequestContext
