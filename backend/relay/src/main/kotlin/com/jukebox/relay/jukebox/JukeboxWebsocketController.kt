package com.jukebox.relay.jukebox

import com.jukebox.core.cache.SharedCache
import com.jukebox.core.dto.JukeboxSession
import com.jukebox.relay.common.cache.CacheService
import com.jukebox.relay.jukebox.dto.AuthSession
import com.jukebox.relay.jukebox.dto.payload.Payload
import com.jukebox.relay.jukebox.dto.payload.PayloadIn
import com.jukebox.relay.jukebox.dto.payload.PayloadOut
import com.jukebox.relay.jukebox.dto.payload.PlaybackPayload
import com.jukebox.relay.jukebox.dto.toAuthSession
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.reactive.asPublisher
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import kotlin.random.Random

@Component
class JukeboxWebsocketController(
    private val cacheService: CacheService,
    private val messagePublisher: MessagePublisher,
    private val objectMapper: ObjectMapper,
    private val jukeboxService: JukeboxService,
) : WebSocketHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun handle(session: WebSocketSession): Mono<Void> =
        session.resolve().flatMap { jukeboxSession ->
            val outbound = session.getOutbound(jukeboxSession) ?: return@flatMap session.reject()
            val inbound = session.getInbound(jukeboxSession)

            val sessionId = jukeboxSession.userId?.toString() ?: "anonymous-${getRandomHash()}"
            mono {
                messagePublisher.broadcast(
                    jukeboxSession,
                    PayloadOut.GuestChange(sessionId, true),
                )
            }.then(
                Mono
                    .firstWithSignal(inbound, outbound)
                    .doFinally { signalType ->
                        // TODO: 잘 끊기는지 확인하기 checked once.
                        println(">>> [Final] Session closed with signal: $signalType, $sessionId")
                    },
            ).then(
                mono {
                    val payload =
                        if (jukeboxSession.userId == jukeboxSession.hostId) {
                            PayloadOut.HostHasLeft()
                        } else {
                            PayloadOut.GuestChange(sessionId, false)
                        }
                    messagePublisher.broadcast(jukeboxSession, payload)
                    messagePublisher.removeStream(jukeboxSession)
                }.then(),
            )
        }

    private fun WebSocketSession.resolve(): Mono<JukeboxSession> {
        val uri = handshakeInfo.uri
        val ticket =
            UriComponentsBuilder
                .fromUri(uri)
                .build()
                .queryParams
                .getFirst("ticket")

        return mono {
            ticket?.let { cacheService.get(SharedCache.JukeboxWebsocketSession(it)) }
        }.switchIfEmpty {
            log.error("Unexpected websocket ticket: $ticket")
            reject().then(Mono.empty())
        }
    }

    private fun WebSocketSession.reject(): Mono<Void> {
        val json = objectMapper.writeValueAsString(PayloadOut.JukeboxUnavailableError())
        val message = textMessage(json)

        return send(Mono.just(message))
            .then(close(CloseStatus.BAD_DATA))
    }

    private fun WebSocketSession.getOutbound(jukeboxSession: JukeboxSession) =
        messagePublisher.getStream(jukeboxSession)?.let { stream ->
            send(
                stream
                    .filter { it.filter(jukeboxSession) }
                    .transformWhile {
                        emit(it) // If host has left, send message to users and close the session.
                        it.event != PayloadOut.HOST_HAS_LEFT
                    }
                    // TODO: 웹소켓 끊는 로직 만들기
                    .map { textMessage(it.payload) }
                    .asPublisher(),
            )
        }

    private fun WebSocketSession.getInbound(jukeboxSession: JukeboxSession): Mono<Void> =
        jukeboxSession.toAuthSession()?.let { authSession ->
            receive()
                .flatMap { msg ->
                    // NOTE: We need to assign it to a variable because `flatMap` collects
                    //      after mono returns, which can happen before [handlePayload] runs.
                    val payloadText = msg.payloadAsText
                    mono { handlePayload(authSession, payloadText) }
                }.then()
        } ?: Mono.never() // Do not receive any message from anonymous user.

    private fun getRandomHash(length: Int = 4): String {
        val bytes = ByteArray(length)
        Random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private suspend fun handlePayload(
        session: AuthSession,
        json: String,
    ) {
        try {
            val payload =
                objectMapper.readValue(json, Payload::class.java)

            when (payload) {
                is PlaybackPayload -> messagePublisher.tryForward(session, payload)
                is PayloadIn.SpotifyTransfer -> jukeboxService.transferSpotifyDevice(session, payload)
                else -> log.error("Unexpected payload: $payload")
            }
        } catch (e: Exception) {
            val error =
                when (e) {
                    is JacksonException -> PayloadOut.BadRequestError()
                    else -> PayloadOut.UnknownError()
                }
            messagePublisher.reply(session, error)
        }
    }
}
