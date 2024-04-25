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
        event = copyWith(type: type)
        return this
    }

    MessageDomainEventDSL withPayload(Map payload) {
        event = copyWith(payload: payload)
        return this
    }

    MessageDomainEventDSL from(UUID userId) {
        event = copyWith(responsibleUserId: userId)
        return this
    }

    private MessageDomainEvent copyWith(Map changes) {
        new MessageDomainEvent(
            (UUID) changes.get('eventId') ?: this.event.eventId,
            (UUID) changes.get('messageId') ?: this.event.messageId,
            (Map<String, Object>) (changes.get('payload') ?: this.event.payload),
            (MessageDomainEventType) changes.get('type') ?: this.event.type,
            (UUID) changes.get('responsibleUserId') ?: this.event.responsibleUserId,
            (UUID) changes.get('operationId') ?: this.event.operationId,
            (Long) changes.get('dateCreated') ?: this.event.dateCreated
        )
    }
}

