package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageRead


import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaDomainEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.MessageReadSaga

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
