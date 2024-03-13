package com.rest_service.saga_orchestrator.infrastructure

import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.time.Instant
import java.util.UUID

@MappedEntity
data class SagaEvent(
    @field:Id
    @AutoPopulated
    val id: UUID? = null,
    val operationId: UUID,
    @MappedProperty(type = DataType.JSON)
    val payload: Map<String, Any>,
    val responsibleService: ServiceEnum,
    val responsibleUserEmail: String,
    val type: SagaType,
    val dateCreated: Long = Instant.now()
        .toEpochMilli(),
)
