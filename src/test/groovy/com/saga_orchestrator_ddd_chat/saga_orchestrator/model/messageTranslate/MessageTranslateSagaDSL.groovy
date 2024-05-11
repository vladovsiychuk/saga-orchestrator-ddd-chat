package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageTranslate


import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaDomainEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.MessageTranslateSaga

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
