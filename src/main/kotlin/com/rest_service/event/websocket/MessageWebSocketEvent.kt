package com.rest_service.event.websocket

import com.rest_service.dto.MessageDTO
import com.rest_service.enums.WebSocketType
import io.micronaut.core.annotation.Introspected

@Introspected
data class MessageWebSocketEvent(
    val data: MessageDTO,
    val type: WebSocketType = WebSocketType.MESSAGE_UPDATE,
)
