package com.jukebox.core.cache

import com.jukebox.core.dto.JukeboxSession
import java.time.Duration

object SharedCache {
    data object JukeboxWebsocketSession :
        CacheConfig<JukeboxSession>("jukebox_session", Duration.ofMinutes(1)) {
        operator fun invoke(ticket: String) = CacheQuery(this, "ticket:$ticket")
    }
}
