package com.rest_service.saga_orchestrator.model.messageRead


import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.MessageReadSaga

class MessageReadSagaDSL {
    MessageReadSaga state = new MessageReadSaga(UUID.randomUUID(), UUID.randomUUID())

    static MessageReadSagaDSL the(MessageReadSagaDSL dsl) {
        return dsl
    }

    static MessageReadSagaDSL aMessageReadSaga() {
        return new MessageReadSagaDSL()
    }


    MessageReadSagaDSL reactsTo(SagaDomainEvent event) {
        state.apply event
        return this
    }

    SagaEvent responseEvent() {
        state.createResponseSagaEvent().block()
    }
}
