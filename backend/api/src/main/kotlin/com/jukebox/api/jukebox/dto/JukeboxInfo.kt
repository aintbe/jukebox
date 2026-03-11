package com.jukebox.api.jukebox.dto

import com.jukebox.api.common.cache.CacheableData

class JukeboxInfo {
    @CacheableData
    data class Detail(
        val id: Long,
        val handle: String,
        val serviceName: String?,
        val hostId: Long,
    )
}
