package com.rest_service.event.websocket

import com.rest_service.dto.RoomDTO
import com.rest_service.enums.WebSocketType
import io.micronaut.core.annotation.Introspected

@Introspected
data class RoomWebSocketEvent(
    val data: RoomDTO,
    val type: WebSocketType = WebSocketType.ROOM_UPDATE,
)
