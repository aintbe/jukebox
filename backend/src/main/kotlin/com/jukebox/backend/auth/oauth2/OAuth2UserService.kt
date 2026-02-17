package com.jukebox.backend.auth.oauth2

import OAuth2Id
import com.jukebox.backend.auth.dto.OAuth2Principal
import com.jukebox.backend.auth.dto.RequestUser
import com.jukebox.backend.oauth2.entity.OAuth2
import com.jukebox.backend.user.UserRepository
import com.jukebox.backend.user.entity.User
import org.apache.commons.logging.LogFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OAuth2UserService(
    private val oAuth2Repository: OAuth2Repository,
    private val userRepository: UserRepository,
) : DefaultOAuth2UserService() {
    private val log = LogFactory.getLog(OAuth2UserService::class.java)

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
            oAuth2Repository.findUserById(provider, providerId)
                ?: signUp(provider, providerId)
        val requestUser = RequestUser.from(user)

        return OAuth2Principal(requestUser, oAuth2User)
    }

    @Transactional
    fun signUp(
        provider: String,
        providerId: String,
    ): User {
        val savedUser = userRepository.save(User(username = "$provider:$providerId"))
        val oAuth2 = OAuth2(id = OAuth2Id(provider, providerId), user = savedUser)
        oAuth2Repository.save(oAuth2)

        return savedUser
    }
}
