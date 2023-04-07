package com.rest_service.websocket

import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Singleton
import java.util.function.Predicate

@Singleton
class WebSocketService(private val broadcaster: WebSocketBroadcaster) {
    fun sendMessageToUser(msg: String, userId: String) {
        broadcaster.broadcastSync(msg, isValid(userId))
    }

    private fun isValid(userId: String): Predicate<WebSocketSession> {
        return Predicate<WebSocketSession> {
            userId.equals(it.uriVariables.get("userId", String::class.java, null), ignoreCase = true)
        }
    }
}
