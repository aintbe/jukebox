package com.jukebox.relay.common.cache

import com.jukebox.core.cache.CacheConfig
import com.jukebox.core.cache.CacheQuery
import com.jukebox.relay.jukebox.dto.JukeboxInfo

object Cache {
    data object OAuth2Client : CacheConfig<JukeboxInfo.CachedStreamingAccess>("oauth2_client") {
        operator fun invoke(hostId: Long) = CacheQuery(this, "user:$hostId")
    }
}
