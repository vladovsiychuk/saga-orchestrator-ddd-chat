package com.rest_service.commons.dto

import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class RoomDTO(
    val id: UUID,
    val name: String?,
    val createdBy: UUID,
    val members: Set<UUID>,
    val dateCreated: Long,
    val dateUpdated: Long,
) : DTO {
    constructor(command: RoomCreateCommand, event: RoomDomainEvent) : this(
        event.roomId,
        null,
        event.responsibleUserId,
        setOf(command.companionId, event.responsibleUserId),
        event.dateCreated,
        event.dateCreated,
    )
}
