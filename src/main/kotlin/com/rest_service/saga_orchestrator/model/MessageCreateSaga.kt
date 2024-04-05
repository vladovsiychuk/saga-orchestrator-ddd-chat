package com.rest_service.saga_orchestrator.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.MessageCreateCommand
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import java.util.UUID

class MessageCreateSaga(
    val operationId: UUID,
    val responsibleUserEmail: String,
    private val responsibleUserId: UUID,
) : AbstractSagaStateManager<MessageCreateCommand, MessageDTO>() {
    private val mapper = jacksonObjectMapper()
    override fun startEvent() = SagaEventType.MESSAGE_CREATE_START
    override fun approveEvent() = SagaEventType.MESSAGE_CREATE_APPROVED
    override fun rejectEvent() = SagaEventType.MESSAGE_CREATE_REJECTED

    override fun transformCommand(payload: Map<String, Any>): MessageCreateCommand =
        mapper.convertValue(payload, MessageCreateCommand::class.java)

    override fun transformDTO(payload: Map<String, Any>): MessageDTO =
        mapper.convertValue(payload, MessageDTO::class.java)

    override fun isComplete() = approvedServices.containsAll(
        listOf(
            ServiceEnum.ROOM_SERVICE, ServiceEnum.MESSAGE_SERVICE
        )
    )

    override fun mainDomainService() = ServiceEnum.MESSAGE_SERVICE

    override fun createInitiatedResponseEvent() =
        SagaEvent(SagaEventType.MESSAGE_CREATE_INITIATED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, command)

    override fun createCompletedResponseEvent() =
        SagaEvent(SagaEventType.MESSAGE_CREATE_COMPLETED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, dto)

    override fun createErrorResponseEvent() =
        SagaEvent(SagaEventType.MESSAGE_CREATE_ERROR, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, errorDto!!)

    override fun apply(event: DomainEvent) = state.apply(event as SagaDomainEvent)
    override fun createResponseSagaEvent() = state.createSagaResponseEvent()
}
