package com.rest_service.saga_orchestrator.web

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class ResponseDTO(
    val operationId: UUID
)
