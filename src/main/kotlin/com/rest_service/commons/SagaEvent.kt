package com.rest_service.commons

import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import java.util.UUID

data class SagaEvent(
    val type: SagaEventType,
    val operationId: UUID,
    val responsibleService: ServiceEnum,
    val responsibleUserEmail: String,
    val responsibleUserId: UUID?,
    val payload: Any,
)
