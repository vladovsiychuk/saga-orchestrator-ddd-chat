package com.rest_service.read_service.entity

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.util.UUID

@MappedEntity
data class RoomView(
    @field:Id
    val id: UUID,
    val name: String?,
    val createdBy: UUID,
    @MappedProperty(type = DataType.JSON)
    val members: Set<UUID>,
    val dateCreated: Long,
    val dateUpdated: Long,
)
