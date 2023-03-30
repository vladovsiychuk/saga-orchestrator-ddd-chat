package com.rest_service.domain

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.Instant
import java.util.UUID

@MappedEntity
data class Room(
    @field:Id
    val id: UUID,
    val name: String?,
    val createdBy: UUID,
    val dateCreated: Long = Instant.now()
        .toEpochMilli(),
    val dateUpdated: Long = Instant.now()
        .toEpochMilli(),
) {
    constructor(id: UUID) : this(
        UUID.randomUUID(),
        null,
        id,
    )
}
