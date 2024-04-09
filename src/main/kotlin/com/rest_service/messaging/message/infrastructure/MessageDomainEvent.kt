package com.rest_service.messaging.message.infrastructure

import com.rest_service.commons.DomainEvent
import com.rest_service.messaging.user.infrastructure.TimeUtils
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.util.UUID

@MappedEntity
data class MessageDomainEvent(
    @field:Id
    @AutoPopulated
    val eventId: UUID? = null,
    val messageId: UUID? = null,
    @MappedProperty(type = DataType.JSON)
    var payload: Map<String, Any>,
    var type: MessageDomainEventType,
    var responsibleUserId: UUID,
    var operationId: UUID,
    val dateCreated: Long = TimeUtils.now(),
) : DomainEvent
