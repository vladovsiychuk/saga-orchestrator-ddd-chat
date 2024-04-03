package com.rest_service.websocket_service.configuration

import java.util.UUID

interface WebSocketService {
    fun sendMessageToUser(msg: String, userId: UUID)
}
