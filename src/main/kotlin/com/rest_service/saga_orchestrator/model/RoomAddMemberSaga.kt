package com.rest_service.saga_orchestrator.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import java.util.UUID

class RoomAddMemberSaga(
    val operationId: UUID,
    private val responsibleUserId: UUID,
) : AbstractSagaStateManager<RoomAddMemberCommand, RoomDTO>() {
    private val mapper = jacksonObjectMapper()
    override fun startEvent() = SagaEventType.ROOM_ADD_MEMBER_START
    override fun approveEvent() = SagaEventType.ROOM_ADD_MEMBER_APPROVED
    override fun rejectEvent() = SagaEventType.ROOM_ADD_MEMBER_REJECTED

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
        SagaEvent(SagaEventType.ROOM_ADD_MEMBER_INITIATED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, command)

    override fun createCompletedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_ADD_MEMBER_COMPLETED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, dto)

    override fun createErrorResponseEvent() =
        SagaEvent(SagaEventType.ROOM_ADD_MEMBER_ERROR, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, errorDto!!)
}
