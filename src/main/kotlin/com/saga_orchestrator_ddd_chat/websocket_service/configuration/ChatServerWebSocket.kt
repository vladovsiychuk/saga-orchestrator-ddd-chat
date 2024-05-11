package com.saga_orchestrator_ddd_chat.websocket_service.configuration

import io.micronaut.security.annotation.Secured
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import java.util.UUID

@Secured("isAuthenticated()")
@ServerWebSocket("/ws/chat/{userId}")
class ChatServerWebSocket(private val service: WebSocketService) {

    @OnOpen
    fun onOpen(userId: UUID) {
    }

    @OnMessage
    fun onMessage(userId: UUID, message: String, session: WebSocketSession) {
        val msg = "[$userId] $message"
        service.sendMessageToUser(msg, userId)
    }

    @OnClose
    fun onClose(userId: UUID) {
        val msg = "[$userId] Disconnected!"
        service.sendMessageToUser(msg, userId)
    }
}
