package com.jukebox.backend.auth.dto

import org.springframework.security.oauth2.core.user.OAuth2User

class OAuth2Principal(
    val authUser: AuthUser,
    private val oAuth2User: OAuth2User,
) : OAuth2User by oAuth2User {
    override fun getName(): String = authUser.userId.toString()

    override fun <A : Any?> getAttribute(name: String?): A? = oAuth2User.getAttribute(name)
}
