package com.saga_orchestrator_ddd_chat.saga_orchestrator.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.command.RoomCreateCommand
import com.saga_orchestrator_ddd_chat.commons.dto.DTO
import com.saga_orchestrator_ddd_chat.commons.dto.RoomDTO
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import java.util.UUID

class RoomCreateSaga(
    val operationId: UUID,
    private val responsibleUserId: UUID,
) : AbstractSagaStateManager<RoomCreateCommand, RoomDTO>() {
    private val mapper = jacksonObjectMapper()
    override fun startEvent() = SagaEventType.ROOM_CREATE_START
    override fun approveEvent() = SagaEventType.ROOM_CREATE_APPROVED
    override fun rejectEvent() = SagaEventType.ROOM_CREATE_REJECTED

    override fun transformCommand(payload: Map<String, Any>): RoomCreateCommand =
        mapper.convertValue(payload, RoomCreateCommand::class.java)

    override fun transformDTO(payload: Map<String, Any>): RoomDTO =
        mapper.convertValue(payload, RoomDTO::class.java)

    override fun isComplete() = approvedServices.containsAll(
        listOf(
            ServiceEnum.ROOM_SERVICE, ServiceEnum.USER_SERVICE
        )
    )

    override fun mainDomainService() = ServiceEnum.ROOM_SERVICE

    override fun createInitiatedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_INITIATED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, command)

    override fun createCompletedResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_COMPLETED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, dto)

    override fun createErrorResponseEvent() =
        SagaEvent(SagaEventType.ROOM_CREATE_ERROR, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserId, errorDto!!)

    override fun toDto(): DTO {
        return object : DTO {}
    }
}
