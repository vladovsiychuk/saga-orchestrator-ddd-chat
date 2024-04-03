package com.rest_service.messaging.user.infrastructure

import com.rest_service.commons.DomainEvent
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
    val userId: UUID? = null,
    var email: String? = null,
    @MappedProperty(type = DataType.JSON)
    var payload: Map<String, Any>,
    var type: UserDomainEventType,
    var operationId: UUID,
    val dateCreated: Long = TimeUtils.now(),
) : DomainEvent
