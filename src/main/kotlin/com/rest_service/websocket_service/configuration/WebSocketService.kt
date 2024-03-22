package com.rest_service.websocket

import java.util.UUID

interface WebSocketService {
    fun sendMessageToUser(msg: String, userId: UUID)
}
