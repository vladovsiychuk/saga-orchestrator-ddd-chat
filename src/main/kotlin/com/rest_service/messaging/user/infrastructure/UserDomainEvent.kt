package com.rest_service.messaging.user.infrastructure

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.TimeUtils
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.util.UUID

@MappedEntity
data class UserDomainEvent(
    @field:Id
    @AutoPopulated
    val eventId: UUID? = null,
    val userId: UUID,
    @MappedProperty(type = DataType.JSON)
    val payload: Map<String, Any>,
    val type: UserDomainEventType,
    val operationId: UUID,
    val responsibleUserId: UUID,
    val dateCreated: Long = TimeUtils.now(),
) : DomainEvent
