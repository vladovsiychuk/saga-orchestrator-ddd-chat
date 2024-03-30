package com.rest_service.saga_orchestrator.web.request

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class RoomCreateRequest(
    val companionUserId: UUID,
)
