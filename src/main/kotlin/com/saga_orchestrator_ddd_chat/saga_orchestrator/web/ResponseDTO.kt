package com.saga_orchestrator_ddd_chat.saga_orchestrator.web

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class ResponseDTO(
    val operationId: UUID
)
