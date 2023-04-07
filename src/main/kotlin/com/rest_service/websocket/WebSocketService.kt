package com.rest_service.websocket

import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Singleton
import java.util.UUID
import java.util.function.Predicate

@Singleton
class WebSocketService(private val broadcaster: WebSocketBroadcaster) {
    fun sendMessageToUser(msg: String, userId: UUID) {
        broadcaster.broadcastSync(msg, isValid(userId))
    }

    private fun isValid(userId: UUID): Predicate<WebSocketSession> {
        return Predicate<WebSocketSession> {
            userId == UUID.fromString(it.uriVariables.get("userId", String::class.java, null))
        }
    }
}
