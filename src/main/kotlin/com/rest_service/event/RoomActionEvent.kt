package com.rest_service.event

import com.rest_service.dto.RoomDTO
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class RoomActionEvent(
    val userId: UUID,
    val room: RoomDTO,
)
