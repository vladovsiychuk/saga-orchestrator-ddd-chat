package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageCreate


import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaDomainEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.MessageCreateSaga

class MessageCreateSagaDSL {
    MessageCreateSaga state = new MessageCreateSaga(UUID.randomUUID(), UUID.randomUUID())

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
