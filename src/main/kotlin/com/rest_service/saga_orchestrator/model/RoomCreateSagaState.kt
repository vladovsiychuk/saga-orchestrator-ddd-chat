package com.rest_service.saga_orchestrator.model

import com.rest_service.commons.command.RoomCommand
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import java.util.UUID

class RoomCreateSagaState(
    private val operationId: UUID,
    private val eventFactory: EventFactory
) : AbstractSagaState() {
    override fun initiateSaga(event: SagaEvent) {
        currentUserEmail = event.responsibleUserEmail
        command = convertEventData(event.payload, RoomCommand::class.java)
        transitionTo(SagaStatus.INITIATED)
    }

    override fun approveSaga(event: SagaEvent) {
        approvedServices.add(event.responsibleService)
        data = convertEventData(event.payload, RoomDTO::class.java)
        transitionTo(if (isComplete()) SagaStatus.COMPLETED else SagaStatus.IN_APPROVING)
    }

    override fun rejectSaga(event: SagaEvent) {
        errorDto = convertEventData(event.payload, ErrorDTO::class.java)
        transitionTo(SagaStatus.REJECTED)
    }

    override fun isComplete(): Boolean {
        return approvedServices.containsAll(listOf(ServiceEnum.ROOM_SERVICE))
    }


    override fun createInitiateEvent() =
        eventFactory.createEvent(SagaType.ROOM_CREATE_INITIATE, operationId, currentUserEmail, command)

    override fun createCompleteEvent() =
        eventFactory.createEvent(SagaType.ROOM_CREATE_COMPLETE, operationId, currentUserEmail, data)

    override fun createErrorEvent() =
        eventFactory.createEvent(SagaType.ROOM_CREATE_ERROR, operationId, currentUserEmail, errorDto!!)
}
