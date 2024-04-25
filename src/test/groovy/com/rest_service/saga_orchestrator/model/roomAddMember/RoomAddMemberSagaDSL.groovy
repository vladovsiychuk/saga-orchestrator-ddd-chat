package com.rest_service.saga_orchestrator.model.roomAddMember


import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.RoomAddMemberSaga

class RoomAddMemberSagaDSL {
    RoomAddMemberSaga state = new RoomAddMemberSaga(UUID.randomUUID(), UUID.randomUUID())

    static RoomAddMemberSagaDSL the(RoomAddMemberSagaDSL dsl) {
        return dsl
    }

    static RoomAddMemberSagaDSL aRoomAddMemberSaga() {
        return new RoomAddMemberSagaDSL()
    }


    RoomAddMemberSagaDSL reactsTo(SagaDomainEvent event) {
        state.apply event
        return this
    }

    SagaEvent responseEvent() {
        state.createResponseSagaEvent().block()
    }
}
