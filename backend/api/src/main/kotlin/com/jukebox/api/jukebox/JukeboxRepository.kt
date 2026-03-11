package com.jukebox.api.jukebox

import com.jukebox.api.jukebox.dto.JukeboxInfo
import com.jukebox.api.jukebox.entity.Jukebox
import com.jukebox.api.streamingservice.entity.StreamingService
import com.jukebox.api.streamingservice.entity.StreamingServiceUser
import com.jukebox.api.user.entity.User
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JukeboxRepository :
    JpaRepository<Jukebox, Long>,
    KotlinJdslJpqlExecutor {
    @Cacheable(value = ["jukebox"], key = "'handle:' + #handle")
    fun findByHandle(handle: String): JukeboxInfo.Detail? =
        findAll {
            selectNew<JukeboxInfo.Detail>(
                path(Jukebox::id),
                path(Jukebox::handle),
                path(StreamingService::name),
                path(User::id),
            ).from(
                entity(Jukebox::class),
                join(Jukebox::host),
                leftJoin(User::streamingServiceUser),
                leftJoin(StreamingServiceUser::service),
            ).where(
                path(Jukebox::handle).eq(handle),
            )
        }.firstOrNull()
}
