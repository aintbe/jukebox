package com.jukebox.backend.user

import com.jukebox.backend.streamingservice.entity.StreamingService
import com.jukebox.backend.streamingservice.entity.StreamingServiceUser
import com.jukebox.backend.streamingservice.entity.StreamingServiceUserPK
import com.jukebox.backend.user.dto.UserQueryDto
import com.jukebox.backend.user.entity.User
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository :
    JpaRepository<User, Long>,
    KotlinJdslJpqlExecutor {
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

    fun findUserProfile(id: Long): UserQueryDto.UserProfile? =
        findAll {
            selectNew<UserQueryDto.UserProfile>(
                path(User::id),
                path(User::username),
                path(StreamingService::name),
            ).from(
                entity(User::class),
                leftJoin(User::streamingServiceUser),
                leftJoin(StreamingServiceUser::service),
            ).where(
                path(User::id).eq(id),
            )
        }.firstOrNull()
}
