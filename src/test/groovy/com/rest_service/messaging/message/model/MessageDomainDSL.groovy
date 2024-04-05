package com.rest_service.messaging.message.model

import com.rest_service.commons.SagaEvent

class MessageDomainDSL {
    MessageDomain domain = new MessageDomain(UUID.randomUUID(), "example@test.com", UUID.randomUUID())

    static MessageDomainDSL aMessage() {
        return new MessageDomainDSL()
    }

    static MessageDomainDSL the(MessageDomainDSL dsl) {
        return dsl
    }

    MessageDomainDSL and() {
        return this
    }

    MessageDomainDSL reactsTo(MessageDomainEventDSL event) {
        domain.apply(event.event)
        return this
    }

    MessageDomainDSL withOperationId(UUID operationId) {
        domain.operationId = operationId
        return this
    }

    MessageDomainDSL withResponsibleUserEmail(String responsibleUserEmail) {
        domain.responsibleUserEmail = responsibleUserEmail
        return this
    }

    SagaEvent responseEvent() {
        return domain.createResponseSagaEvent().block()
    }
}
