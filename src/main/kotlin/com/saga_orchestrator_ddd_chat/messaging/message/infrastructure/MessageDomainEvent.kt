package com.saga_orchestrator_ddd_chat.messaging.message.infrastructure

import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.TimeUtils
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
    val messageId: UUID,
    @MappedProperty(type = DataType.JSON)
    val payload: Map<String, Any>,
    val type: MessageDomainEventType,
    val responsibleUserId: UUID,
    val operationId: UUID,
    val dateCreated: Long = TimeUtils.now(),
) : DomainEvent
