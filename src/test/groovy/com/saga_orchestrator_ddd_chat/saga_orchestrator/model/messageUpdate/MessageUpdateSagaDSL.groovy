package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageUpdate


import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaDomainEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.MessageUpdateSaga

class MessageUpdateSagaDSL {
    MessageUpdateSaga state = new MessageUpdateSaga(UUID.randomUUID(), UUID.randomUUID())

    static MessageUpdateSagaDSL the(MessageUpdateSagaDSL dsl) {
        return dsl
    }

    static MessageUpdateSagaDSL aMessageUpdateSaga() {
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
