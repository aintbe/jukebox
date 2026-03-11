package com.jukebox.api.common.cache

import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Data classes are always final, which means Serializer
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
annotation class CacheableData
