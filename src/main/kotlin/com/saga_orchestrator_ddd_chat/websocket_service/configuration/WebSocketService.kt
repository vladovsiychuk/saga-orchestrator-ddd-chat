package com.saga_orchestrator_ddd_chat.websocket_service.configuration

import java.util.UUID
import reactor.core.publisher.Mono

interface WebSocketService {
    fun sendMessageToUser(msg: String, userId: UUID): Mono<String>
}
