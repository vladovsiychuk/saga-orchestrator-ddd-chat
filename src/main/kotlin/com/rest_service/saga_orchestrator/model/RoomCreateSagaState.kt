package com.rest_service.saga_orchestrator.model

import com.rest_service.commons.command.RoomCommand
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import java.util.UUID
import reactor.core.publisher.Mono

class RoomCreateSagaState(
    private val operationId: UUID,
    private val eventFactory: EventFactory
) : AbstractSagaState() {
    override fun initiateSaga(event: SagaEvent): Mono<Boolean> {
        currentUserEmail = event.responsibleUserEmail
        command = convertEventData(event.payload, RoomCommand::class.java)
        return transitionTo(SagaStatus.INITIATED)
    }

    override fun approveSaga(event: SagaEvent): Mono<Boolean> {
        approvedServices.add(event.responsibleService)
        data = convertEventData(event.payload, RoomDTO::class.java)
        return transitionTo(if (isComplete()) SagaStatus.COMPLETED else SagaStatus.IN_APPROVING)
    }

    override fun rejectSaga(event: SagaEvent): Mono<Boolean> {
        errorDto = convertEventData(event.payload, ErrorDTO::class.java)
        return transitionTo(SagaStatus.REJECTED)
    }

    override fun isComplete(): Boolean {
        return approvedServices.containsAll(listOf(ServiceEnum.ROOM_SERVICE))
    }


    override fun createInitiateEvent() =
        eventFactory.createEvent(EventType.ROOM_CREATE_INITIATE, operationId, currentUserEmail, command)

    override fun createCompleteEvent() =
        eventFactory.createEvent(EventType.ROOM_CREATE_COMPLETE, operationId, currentUserEmail, data)

    override fun createErrorEvent() =
        eventFactory.createEvent(EventType.ROOM_CREATE_ERROR, operationId, currentUserEmail, errorDto!!)
}
