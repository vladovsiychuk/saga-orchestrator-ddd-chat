package com.rest_service.websocket

import io.micronaut.security.annotation.Secured
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket

@Secured("isAuthenticated()")
@ServerWebSocket("/ws/chat/{userId}")
class ChatServerWebSocket(private val service: WebSocketService) {

    @OnOpen
    fun onOpen(userId: String) {
        val msg = "[$userId] Joined!"
        service.sendMessageToUser(msg, userId)
    }

    @OnMessage
    fun onMessage(userId: String, message: String, session: WebSocketSession) {
        val msg = "[$userId] $message"
        service.sendMessageToUser(msg, userId)
    }

    @OnClose
    fun onClose(userId: String) {
        val msg = "[$userId] Disconnected!"
        service.sendMessageToUser(msg, userId)
    }
}
