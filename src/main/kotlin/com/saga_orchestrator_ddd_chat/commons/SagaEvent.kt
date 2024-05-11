package com.saga_orchestrator_ddd_chat.commons

import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import java.util.UUID

data class SagaEvent(
    val type: SagaEventType,
    val operationId: UUID,
    val responsibleService: ServiceEnum,
    val responsibleUserId: UUID,
    val payload: Any,
)
