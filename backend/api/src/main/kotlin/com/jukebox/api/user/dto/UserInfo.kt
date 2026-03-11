package com.jukebox.api.user.dto

class UserInfo {
    class UserProfile private constructor(
        val id: Long,
        val username: String,
        val streamingServiceName: String?,
        val jukebox: Jukebox?,
    ) {
        constructor(
            id: Long,
            username: String,
            streamingServiceName: String?,
            jukeboxId: Long?,
            jukeboxHandle: String?,
        ) : this(
            id,
            username,
            streamingServiceName,
            jukeboxId?.let { id ->
                if (jukeboxHandle.isNullOrBlank()) {
                    null
                } else {
                    Jukebox(id, jukeboxHandle)
                }
            },
        )
    }

    data class Jukebox(
        val id: Long,
        val handle: String,
    )
}
