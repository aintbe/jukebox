package com.jukebox.api.user

import com.jukebox.api.jukebox.entity.Jukebox
import com.jukebox.api.streamingservice.entity.StreamingService
import com.jukebox.api.streamingservice.entity.StreamingServiceUser
import com.jukebox.api.streamingservice.entity.StreamingServiceUserPK
import com.jukebox.api.user.dto.UserInfo
import com.jukebox.api.user.entity.User
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository :
    JpaRepository<User, Long>,
    KotlinJdslJpqlExecutor {
    fun findUserById(id: Long): User?

    fun findUserByOAuth2(
        provider: String,
        providerId: String,
    ): User? =
        findAll {
            select(path(StreamingServiceUser::user))
                .from(
                    entity(StreamingServiceUser::class),
                    join(StreamingServiceUser::service),
                ).whereAnd(
                    path(StreamingService::name).eq(provider),
                    path(StreamingServiceUser::pk)(StreamingServiceUserPK::serviceUserId).eq(providerId),
                )
        }.firstOrNull()

    fun findUserProfile(id: Long): UserInfo.UserProfile? =
        findAll {
            selectNew<UserInfo.UserProfile>(
                path(User::id),
                path(User::username),
                path(StreamingService::name),
                path(Jukebox::id),
                path(Jukebox::handle),
            ).from(
                entity(User::class),
                leftJoin(User::streamingServiceUser),
                leftJoin(StreamingServiceUser::service),
                leftJoin(Jukebox::class).on(
                    path(User::id).eq(path(Jukebox::host)(User::id)),
                ),
            ).where(
                path(User::id).eq(id),
            )
        }.firstOrNull()
}
