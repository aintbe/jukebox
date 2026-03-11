package com.jukebox.api.auth.dto

class AuthDto {
    data class IssueRequest(
        val ticket: String,
    )
}
