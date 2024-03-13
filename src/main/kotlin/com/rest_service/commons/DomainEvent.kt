package com.rest_service.commons

import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import java.util.UUID

data class DomainEvent(
    val type: SagaType,
    val operationId: UUID,
    val responsibleService: ServiceEnum,
    val responsibleUserEmail: String,
    val payload: Any,
)
