package com.rest_service.saga_orchestrator.model.messageCreate

import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.MessageCreateSaga

class MessageCreateSagaDSL {
    MessageCreateSaga state = new MessageCreateSaga(UUID.randomUUID(), "example@test.com", UUID.randomUUID())

    static MessageCreateSagaDSL the(MessageCreateSagaDSL dsl) {
        return dsl
    }

    static MessageCreateSagaDSL aMessageSaga() {
        return new MessageCreateSagaDSL()
    }


    MessageCreateSagaDSL reactsTo(SagaDomainEvent event) {
        state.apply event
        return this
    }

    SagaEvent responseEvent() {
        state.createResponseSagaEvent().block()
    }
}
