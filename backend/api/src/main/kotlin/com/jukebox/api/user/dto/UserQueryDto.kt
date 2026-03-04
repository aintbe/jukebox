package com.jukebox.api.user.dto

class UserQueryDto {
    data class UserProfile(
        val id: Long,
        val username: String,
        val streamingServiceName: String?,
        val jukebox: JukeboxDetail?,
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
                    JukeboxDetail(id, jukeboxHandle)
                }
            },
        )
    }

    data class JukeboxDetail(
        val id: Long,
        val handle: String,
    )
}
