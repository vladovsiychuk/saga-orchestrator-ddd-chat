package com.rest_service.entity

import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.Instant
import java.util.UUID

@MappedEntity
data class Room(
    @field:Id
    @AutoPopulated
    val id: UUID? = null,
    val name: String? = null,
    val createdBy: UUID,
    val dateCreated: Long = Instant.now()
        .toEpochMilli(),
    val dateUpdated: Long = Instant.now()
        .toEpochMilli(),
)
