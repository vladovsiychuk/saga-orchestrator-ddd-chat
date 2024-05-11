package com.saga_orchestrator_ddd_chat.websocket_service

import com.saga_orchestrator_ddd_chat.commons.dto.DTO
import io.micronaut.core.annotation.Introspected

@Introspected
data class WebSocketEvent(
    val data: DTO,
    val type: WebSocketType,
)
