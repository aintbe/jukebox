package com.jukebox.api.auth.dto

class AuthInfo {
    data class Tokens(
        val accessToken: String,
        val refreshToken: String,
    )
}
