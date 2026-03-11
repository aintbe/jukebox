package com.jukebox.relay.jukebox

import com.jukebox.core.dto.JukeboxSession
import com.jukebox.relay.jukebox.dto.AuthSession
import com.jukebox.relay.jukebox.dto.WebsocketMessage
import com.jukebox.relay.jukebox.dto.payload.Payload
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap

@Service
class MessagePublisher(
    private val objectMapper: ObjectMapper,
) {
    private val streams = ConcurrentHashMap<Long, MutableSharedFlow<WebsocketMessage>>()

    fun getStream(session: JukeboxSession): SharedFlow<WebsocketMessage>? {
        val stream =
            // Hosts are allowed to create new stream.
            if (session.hostId == session.userId) {
                streams
                    .getOrPut(session.jukeboxId) {
                        MutableSharedFlow(
                            extraBufferCapacity = 256,
                            onBufferOverflow = BufferOverflow.DROP_OLDEST,
                        )
                    }
            } else {
                streams[session.jukeboxId]
            }

        // No longer mutable; other services cannot call `.emit` with this.
        return stream?.asSharedFlow()
    }

    fun removeStream(session: JukeboxSession) {
        // Clean it up when all users of a single jukebox are gone.
        if (streams[session.jukeboxId]?.subscriptionCount?.value == 0) {
            streams.remove(session.jukeboxId)
        }
    }

    fun Payload.toMessage(filter: (receiver: JukeboxSession) -> Boolean) =
        WebsocketMessage(
            filter = filter,
            event = event,
            payload = objectMapper.writeValueAsString(this),
        )

    suspend fun broadcast(
        session: JukeboxSession,
        payload: Payload,
    ) = streams[session.jukeboxId]?.emit(
        payload.toMessage { true },
    )

    suspend fun reply(
        session: AuthSession,
        payload: Payload,
    ) = streams[session.jukeboxId]?.emit(
        payload.toMessage { receiver -> session.userId == receiver.userId },
    )

    suspend fun forward(
        session: AuthSession,
        payload: Payload,
    ) = streams[session.jukeboxId]?.emit(
        payload.toMessage { receiver -> session.userId != receiver.userId },
    )

    /**
     * Use this method only for the messages which are ok to be dropped occasionally.
     */
    fun tryForward(
        session: AuthSession,
        payload: Payload,
    ) = streams[session.jukeboxId]?.tryEmit(
        payload.toMessage { receiver -> session.userId != receiver.userId },
    )
}
