package com.saga_orchestrator_ddd_chat.websocket_service.configuration

import java.util.UUID

interface WebSocketService {
    fun sendMessageToUser(msg: String, userId: UUID)
}
