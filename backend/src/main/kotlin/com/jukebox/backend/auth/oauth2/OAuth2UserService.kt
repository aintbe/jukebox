package com.jukebox.backend.auth.oauth2

import com.jukebox.backend.auth.dto.AuthUser
import com.jukebox.backend.auth.dto.OAuth2Principal
import com.jukebox.backend.streamingservice.StreamingServiceRepository
import com.jukebox.backend.streamingservice.StreamingServiceUserRepository
import com.jukebox.backend.streamingservice.entity.StreamingServiceUser
import com.jukebox.backend.streamingservice.entity.StreamingServiceUserPK
import com.jukebox.backend.user.UserRepository
import com.jukebox.backend.user.entity.User
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OAuth2UserService(
    private val userRepository: UserRepository,
    private val ssRepository: StreamingServiceRepository,
    private val ssUserRepository: StreamingServiceUserRepository,
) : DefaultOAuth2UserService() {
    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2Principal {
        val oAuth2User = super.loadUser(userRequest)
        val provider = userRequest.clientRegistration.registrationId
        val providerId =
            when (provider) {
                "spotify" -> {
                    oAuth2User.name
                }

                // NOTE: [OAuth2FailureHandler.onAuthenticationFailure] can only intercept
                // [AuthenticationException]. Do not change this to business exception.
                else -> {
                    throw OAuth2AuthenticationException("UNSUPPORTED_PROVIDER")
                }
            }

        val user =
            userRepository.findUserByOAuth2(provider, providerId)
                ?: signUp(provider, providerId)
        val authUser = AuthUser.from(user)

        return OAuth2Principal(authUser, oAuth2User)
    }

    @Transactional
    fun signUp(
        provider: String,
        providerId: String,
    ): User {
        val savedUser = userRepository.save(User(username = "$provider:$providerId"))
        val streamingService = ssRepository.findByName(provider)

        val streamingServiceUser =
            StreamingServiceUser(
                pk = StreamingServiceUserPK(streamingService.id, providerId),
                service = streamingService,
                user = savedUser,
            )
        ssUserRepository.save(streamingServiceUser)

        return savedUser
    }
}
