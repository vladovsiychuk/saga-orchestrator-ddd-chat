package com.rest_service.messaging.message.model

import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType

class MessageDomainEventDSL {
    MessageDomainEvent event = new MessageDomainEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        [:],
        MessageDomainEventType.MESSAGE_CREATED,
        UUID.randomUUID(),
        UUID.randomUUID(),
        123123
    )

    static MessageDomainEventDSL anEvent() {
        return new MessageDomainEventDSL()
    }

    static MessageDomainEventDSL the(MessageDomainEventDSL dsl) {
        return dsl
    }

    MessageDomainEventDSL and() {
        return this
    }

    MessageDomainEventDSL ofType(MessageDomainEventType type) {
        event.type = type
        return this
    }

    MessageDomainEventDSL withPayload(Map payload) {
        event.payload = payload
        return this
    }

    MessageDomainEventDSL withOperationId(UUID operationId) {
        event.operationId = operationId
        return this
    }

    MessageDomainEventDSL from(UUID userId) {
        event.responsibleUserId = userId
        return this
    }
}

