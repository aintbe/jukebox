package com.jukebox.api.auth.dto

enum class UserRole {
    TODO, // TODO: add actual roles
    ;

    companion object {
        fun from(value: String): UserRole =
            entries.find {
                it.name.equals(value, ignoreCase = true)
            } ?: TODO
    }
}
