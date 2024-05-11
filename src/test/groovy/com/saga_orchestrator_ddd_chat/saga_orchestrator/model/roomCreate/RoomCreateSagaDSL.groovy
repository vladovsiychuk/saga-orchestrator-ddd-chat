package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.roomCreate


import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaDomainEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.RoomCreateSaga

class RoomCreateSagaDSL {
    RoomCreateSaga state = new RoomCreateSaga(UUID.randomUUID(), UUID.randomUUID())

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
