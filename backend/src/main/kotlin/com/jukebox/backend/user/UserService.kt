package com.jukebox.backend.user

import com.jukebox.backend.common.exception.UnauthorizedException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getUserProfile(id: Long) =
        userRepository.findUserProfile(id)
            ?: throw UnauthorizedException("USER_NOT_FOUND", "Could not find user (id=$id)")
}
