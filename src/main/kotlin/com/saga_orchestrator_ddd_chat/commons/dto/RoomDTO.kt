package com.saga_orchestrator_ddd_chat.commons.dto

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class RoomDTO(
    val id: UUID,
    val name: String?,
    val createdBy: UUID,
    val members: MutableSet<UUID>,
    val dateCreated: Long,
    val dateUpdated: Long,
) : DTO
