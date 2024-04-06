package com.rest_service.saga_orchestrator.model.messageUpdate

import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.MessageUpdateSaga

class MessageUpdateSagaDSL {
    MessageUpdateSaga state = new MessageUpdateSaga(UUID.randomUUID(), "example@test.com", UUID.randomUUID())

    static MessageUpdateSagaDSL the(MessageUpdateSagaDSL dsl) {
        return dsl
    }

    static MessageUpdateSagaDSL aMessageSaga() {
        return new MessageUpdateSagaDSL()
    }


    MessageUpdateSagaDSL reactsTo(SagaDomainEvent event) {
        state.apply event
        return this
    }

    SagaEvent responseEvent() {
        state.createResponseSagaEvent().block()
    }
}
