package com.rest_service.saga_orchestrator.model.roomCreate

import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.RoomCreateSaga


class RoomCreateSagaDSL {
    RoomCreateSaga state = new RoomCreateSaga(UUID.randomUUID(), "example@test.com", UUID.randomUUID())

    static RoomCreateSagaDSL the(RoomCreateSagaDSL dsl) {
        return dsl
    }

    static RoomCreateSagaDSL aRoomSaga() {
        return new RoomCreateSagaDSL()
    }


    RoomCreateSagaDSL reactsTo(SagaDomainEvent event) {
        state.apply event
        return this
    }

    SagaEvent responseEvent() {
        state.createResponseSagaEvent().block()
    }
}