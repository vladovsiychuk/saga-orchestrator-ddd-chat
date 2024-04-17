package com.rest_service.saga_orchestrator.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.MessageCreateCommand
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import java.util.UUID

class MessageCreateSaga(
    val operationId: UUID,
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
        SagaEvent(SagaEventType.MESSAGE_CREATE_INITIATED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, command)

    override fun createCompletedResponseEvent() =
        SagaEvent(SagaEventType.MESSAGE_CREATE_COMPLETED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, dto)

    override fun createErrorResponseEvent() =
        SagaEvent(SagaEventType.MESSAGE_CREATE_ERROR, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, errorDto!!)
}
