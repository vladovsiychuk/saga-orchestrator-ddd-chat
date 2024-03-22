package com.rest_service.websocket_service

import com.rest_service.commons.dto.DTO
import io.micronaut.core.annotation.Introspected

@Introspected
data class WebSocketEvent(
    val data: DTO,
    val type: WebSocketType,
)
