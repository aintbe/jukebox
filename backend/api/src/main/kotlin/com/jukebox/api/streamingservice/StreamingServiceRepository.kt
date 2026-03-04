package com.jukebox.api.streamingservice

import com.jukebox.api.streamingservice.entity.StreamingService
import com.jukebox.api.streamingservice.entity.StreamingServiceUser
import com.jukebox.api.user.entity.User
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository

interface StreamingServiceRepository :
    JpaRepository<StreamingService, Int>,
    KotlinJdslJpqlExecutor {
    @Cacheable(value = ["streaming_service"], key = "'name:' + #name")
    fun findByName(name: String): StreamingService

    @Cacheable(value = ["streaming_service"], key = "'user:' + #userId")
    fun findByUserId(userId: Long): StreamingService? =
        findAll {
            select(
                entity(StreamingService::class),
            ).from(
                entity(StreamingServiceUser::class),
                join(StreamingServiceUser::service),
            ).where(
                path(StreamingServiceUser::user)(User::id).eq(userId),
            )
        }.firstOrNull()
}
