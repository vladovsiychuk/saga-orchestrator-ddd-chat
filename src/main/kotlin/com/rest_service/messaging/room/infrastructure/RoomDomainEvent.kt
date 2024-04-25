package com.rest_service.messaging.room.infrastructure

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.TimeUtils
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.util.UUID

@MappedEntity
data class RoomDomainEvent(
    @field:Id
    @AutoPopulated
    val eventId: UUID? = null,
    val roomId: UUID,
    @MappedProperty(type = DataType.JSON)
    var payload: Map<String, Any>,
    var type: RoomDomainEventType,
    var responsibleUserId: UUID,
    var operationId: UUID,
    val dateCreated: Long = TimeUtils.now(),
) : DomainEvent
