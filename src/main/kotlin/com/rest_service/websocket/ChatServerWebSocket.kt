package com.rest_service.websocket

import io.micronaut.security.annotation.Secured
import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket

import java.util.function.Predicate

@Secured("isAuthenticated()")
@ServerWebSocket("/ws/chat/{userId}")
class ChatServerWebSocket(private val broadcaster: WebSocketBroadcaster) {

    @OnOpen
    fun onOpen(userId: String) {
        val msg = "[$userId] Joined!"
        broadcaster.broadcastSync(msg, isValid(userId))
    }

    @OnMessage
    fun onMessage(userId: String, message: String, session: WebSocketSession) {
        val msg = "[$userId] $message"
        broadcaster.broadcastSync(msg, isValid(userId))
    }

    @OnClose
    fun onClose(userId: String) {
        val msg = "[$userId] Disconnected!"
        broadcaster.broadcastSync(msg, isValid(userId))
    }

    private fun isValid(userId: String): Predicate<WebSocketSession> {
        return Predicate<WebSocketSession> {
            userId.equals(it.uriVariables.get("userId", String::class.java, null), ignoreCase = true)
        }
    }
}
