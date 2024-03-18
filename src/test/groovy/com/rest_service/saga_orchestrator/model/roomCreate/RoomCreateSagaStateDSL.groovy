package com.rest_service.saga_orchestrator.model.roomCreate

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import com.rest_service.saga_orchestrator.model.RoomCreateSagaState
import com.rest_service.saga_orchestrator.model.SagaStatus

import static com.rest_service.saga_orchestrator.model.SagaEventDSL.anEvent

class RoomCreateSagaStateDSL {
    RoomCreateSagaState state

    static RoomCreateSagaStateDSL the(RoomCreateSagaStateDSL dsl) {
        return dsl
    }

    RoomCreateSagaStateDSL(EventFactory eventFactory) {
        state = new RoomCreateSagaState(UUID.randomUUID(), eventFactory)
    }

    RoomCreateSagaStateDSL withStatus(SagaStatus status) {
        switch (status) {
            case SagaStatus.READY:
                // No additional actions required.
                break
            case SagaStatus.INITIATED:
                state.apply(anEvent() withAnyValidRoomCommand() and() ofType EventType.ROOM_CREATE_START event)
                break
            case SagaStatus.COMPLETED:
                state.apply(anEvent() from ServiceEnum.ROOM_SERVICE withAnyValidRoomDTO() and() ofType EventType.ROOM_CREATE_APPROVE event)
                break
        }

        return this
    }

    RoomCreateSagaStateDSL reactsTo(SagaEvent event) {
        state.apply event
        return this
    }

    DomainEvent nextEvent() {
        state.createNextEvent().block()
    }

    Boolean hasStatus(SagaStatus status) {
        return state.status == status
    }
}
