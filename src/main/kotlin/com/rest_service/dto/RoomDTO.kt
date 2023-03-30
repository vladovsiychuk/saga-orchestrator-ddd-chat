package com.rest_service.dto

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class RoomDTO (
    val id: UUID,
    val name: String?,
    val createdBy: UUID,
    val dateCreated: Long,
    val dateUpdated: Long,
)
