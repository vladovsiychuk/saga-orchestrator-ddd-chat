package com.rest_service.saga_orchestrator.model.messageTranslate


import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.MessageTranslateSaga

class MessageTranslateSagaDSL {
    MessageTranslateSaga state = new MessageTranslateSaga(UUID.randomUUID(), UUID.randomUUID())

    static MessageTranslateSagaDSL the(MessageTranslateSagaDSL dsl) {
        return dsl
    }

    static MessageTranslateSagaDSL aMessageTranslateSaga() {
        return new MessageTranslateSagaDSL()
    }


    MessageTranslateSagaDSL reactsTo(SagaDomainEvent event) {
        state.apply event
        return this
    }

    SagaEvent responseEvent() {
        state.createResponseSagaEvent().block()
    }
}
