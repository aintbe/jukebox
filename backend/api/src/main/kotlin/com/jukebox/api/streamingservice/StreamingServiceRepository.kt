package com.jukebox.api.streamingservice

import com.jukebox.api.streamingservice.entity.StreamingService
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository

interface StreamingServiceRepository : JpaRepository<StreamingService, Int> {
    @Cacheable(value = ["streaming-service"], key = "#name")
    fun findByName(name: String): StreamingService
}
