package com.jukebox.api.user

import com.jukebox.core.exception.UserNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getUserProfile(id: Long) =
        userRepository.findUserProfile(id)
            ?: throw UserNotFoundException(id)
}
