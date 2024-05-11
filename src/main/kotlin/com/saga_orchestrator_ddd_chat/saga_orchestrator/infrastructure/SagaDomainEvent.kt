package com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure

import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.time.Instant
import java.util.UUID

@MappedEntity
data class SagaDomainEvent(
    @field:Id
    @AutoPopulated
    val id: UUID? = null,
    var operationId: UUID,
    @MappedProperty(type = DataType.JSON)
    var payload: Map<String, Any>,
    var responsibleService: ServiceEnum,
    val responsibleUserId: UUID,
    var type: SagaEventType,
    val dateCreated: Long = Instant.now()
        .toEpochMilli(),
) : DomainEvent
