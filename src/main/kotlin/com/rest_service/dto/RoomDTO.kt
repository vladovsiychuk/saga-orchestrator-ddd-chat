package com.rest_service.dto

import com.rest_service.domain.Room
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class RoomDTO(
    val id: UUID,
    val name: String?,
    val createdBy: UUID,
    val members: List<UUID>,
    val dateCreated: Long,
    val dateUpdated: Long,
) {
    constructor(room: Room, members: List<UUID>) : this(
        room.id!!,
        room.name,
        room.createdBy,
        members,
        room.dateCreated,
        room.dateUpdated,
    )
}
