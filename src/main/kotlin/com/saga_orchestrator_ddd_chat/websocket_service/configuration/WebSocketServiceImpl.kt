package com.saga_orchestrator_ddd_chat.websocket_service.configuration

import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Singleton
import java.util.UUID
import java.util.function.Predicate
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class WebSocketServiceImpl(private val broadcaster: WebSocketBroadcaster) : WebSocketService {
    override fun sendMessageToUser(msg: String, userId: UUID): Mono<String> {
        return broadcaster.broadcast(msg, isValid(userId)).toMono()
    }

    private fun isValid(userId: UUID): Predicate<WebSocketSession> {
        return Predicate<WebSocketSession> {
            userId == UUID.fromString(it.uriVariables.get("userId", String::class.java, null))
        }
    }
}
