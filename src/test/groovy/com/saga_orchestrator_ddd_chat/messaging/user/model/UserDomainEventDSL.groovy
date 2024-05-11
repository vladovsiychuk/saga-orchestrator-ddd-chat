package com.saga_orchestrator_ddd_chat.messaging.user.model


import com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEvent
import com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventType

class UserDomainEventDSL {
    UserDomainEvent event = new UserDomainEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        [:],
        UserDomainEventType.USER_CREATED,
        UUID.randomUUID(),
        UUID.nameUUIDFromBytes("example@test.com".bytes),
        123123
    )

    static UserDomainEventDSL anEvent() {
        return new UserDomainEventDSL()
    }

    static UserDomainEventDSL the(UserDomainEventDSL dsl) {
        return dsl
    }

    UserDomainEventDSL and() {
        return this
    }

    UserDomainEventDSL ofType(UserDomainEventType type) {
        event = copyWith(type: type)
        return this
    }

    UserDomainEventDSL withPayload(Map payload) {
        event = copyWith(payload: payload)
        return this
    }

    private UserDomainEvent copyWith(Map changes) {
        new UserDomainEvent(
            (UUID) changes.get('eventId') ?: this.event.eventId,
            (UUID) changes.get('userId') ?: this.event.userId,
            (Map<String, Object>) (changes.get('payload') ?: this.event.payload),
            (UserDomainEventType) changes.get('type') ?: this.event.type,
            (UUID) changes.get('operationId') ?: this.event.operationId,
            (UUID) changes.get('responsibleUserId') ?: this.event.responsibleUserId,
            (Long) changes.get('dateCreated') ?: this.event.dateCreated
        )
    }
}
