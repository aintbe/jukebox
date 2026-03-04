package com.jukebox.api.jukebox.web

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Annotation that is used to resolve [com.jukebox.core.dto.RequestContext]
 * to a method argument. To use this in controller, register [RequestContextResolver]
 * to [WebMvcConfigurer].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentRequestContext
