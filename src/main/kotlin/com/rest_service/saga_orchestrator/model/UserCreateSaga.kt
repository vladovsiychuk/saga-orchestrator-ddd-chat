package com.rest_service.saga_orchestrator.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.UserCreateCommand
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import java.util.UUID

class UserCreateSaga(
    val operationId: UUID,
    val responsibleUserEmail: String
) : AbstractSagaStateManager<UserCreateCommand, UserDTO>() {
    private val mapper = jacksonObjectMapper()
    override fun startEvent() = SagaEventType.USER_CREATE_START
    override fun approveEvent() = SagaEventType.USER_CREATE_APPROVE

    override fun transformCommand(payload: Map<String, Any>): UserCreateCommand =
        mapper.convertValue(payload, UserCreateCommand::class.java)

    override fun transformDTO(payload: Map<String, Any>): UserDTO =
        mapper.convertValue(payload, UserDTO::class.java)

    override fun isComplete() = approvedServices.contains(ServiceEnum.USER_SERVICE)
    override fun mainDomainService() = ServiceEnum.USER_SERVICE

    override fun createInitiatedResponseEvent() =
        SagaEvent(SagaEventType.USER_CREATE_INITIATE, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, null, command)

    override fun createCompletedResponseEvent() =
        SagaEvent(SagaEventType.USER_CREATE_COMPLETE, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, null, dto)

    override fun apply(event: DomainEvent) = state.apply(event as SagaDomainEvent)
    override fun createResponseSagaEvent() = state.createSagaResponseEvent()
}
