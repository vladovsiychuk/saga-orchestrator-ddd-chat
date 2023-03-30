package com.rest_service.domain

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID

@MappedEntity
data class Room(
    @field:Id
    val id: UUID,
    val name: String?,
    val createdBy: UUID,
    val dateCreated: Long,
    val dateUpdated: Long,
)
