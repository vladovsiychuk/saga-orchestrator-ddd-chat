package com.saga_orchestrator_ddd_chat.websocket_service.configuration

import io.micronaut.security.annotation.Secured
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import java.util.UUID
import reactor.core.publisher.Mono

@Secured("isAuthenticated()")
@ServerWebSocket("/ws/chat/{userId}")
class ChatServerWebSocket(private val service: WebSocketService) {

    @OnOpen
    fun onOpen(userId: UUID, session: WebSocketSession) {
    }

    @OnMessage
    fun onMessage(userId: UUID, message: String, session: WebSocketSession): Mono<String> {
        val msg = "[$userId] $message"

        return service.sendMessageToUser(msg, userId)
    }

    @OnClose
    fun onClose(userId: UUID): Mono<String> {
        val msg = "[$userId] Disconnected!"
        return service.sendMessageToUser(msg, userId)
    }
}
