package com.rest_service.commons

import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import java.util.UUID

data class DomainEvent(
    val type: EventType,
    val operationId: UUID,
    val responsibleService: ServiceEnum,
    val responsibleUserEmail: String,
    val payload: Any,
)
