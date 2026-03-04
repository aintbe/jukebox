package com.jukebox.api.auth.dto

class AuthDto {
    data class IssueRequest(
        val code: String,
    )

    data class Tokens(
        val accessToken: String,
        val refreshToken: String,
    )
}
