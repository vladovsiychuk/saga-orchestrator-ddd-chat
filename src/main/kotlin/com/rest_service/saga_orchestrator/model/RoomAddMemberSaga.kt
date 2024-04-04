package com.rest_service.saga_orchestrator.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import java.util.UUID

class RoomAddMemberSaga(
    val operationId: UUID,
    val responsibleUserEmail: String,
    private val responsibleUserId: UUID,
) : AbstractSagaStateManager<RoomAddMemberCommand, RoomDTO>() {
    private val mapper = jacksonObjectMapper()
    override fun startEvent() = SagaEventType.ROOM_ADD_MEMBER_START
    override fun approveEvent() = SagaEventType.ROOM_ADD_MEMBER_APPROVED

    override fun transformCommand(payload: Map<String, Any>): RoomAddMemberCommand =
        mapper.convertValue(payload, RoomAddMemberCommand::class.java)

    override fun transformDTO(payload: Map<String, Any>): RoomDTO =
        mapper.convertValue(payload, RoomDTO::class.java)

    override fun isComplete() = approvedServices.containsAll(
        listOf(
            ServiceEnum.ROOM_SERVICE, ServiceEnum.USER_SERVICE
        )
    )

    override fun mainDomainService() = ServiceEnum.ROOM_SERVICE

    override fun createInitiatedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_ADD_MEMBER_INITIATED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, command)

    override fun createCompletedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_ADD_MEMBER_COMPLETED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, dto)

    override fun apply(event: DomainEvent) = state.apply(event as SagaDomainEvent)
    override fun createResponseSagaEvent() = state.createSagaResponseEvent()
}
