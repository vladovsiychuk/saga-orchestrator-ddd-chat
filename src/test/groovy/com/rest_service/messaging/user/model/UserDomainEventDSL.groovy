package com.rest_service.messaging.user.model


import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType

class UserDomainEventDSL {
    UserDomainEvent event = new UserDomainEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "example@test.com",
        [:],
        UserDomainEventType.USER_CREATED,
        UUID.fromString("423ec267-5523-448f-ad18-d3204dfa3f08"),
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
        return this
    }

    UserDomainEventDSL from(String userEmail) {
        event.email = userEmail
        return this
    }

    UserDomainEventDSL withOperationId(UUID operationId) {
        event.operationId = operationId
        return this
    }

    private UserDomainEvent copyWith(Map changes) {
        new UserDomainEvent(
            (UUID) changes.get('eventId') ?: this.event.eventId,
            (UUID) changes.get('userId') ?: this.event.userId,
            (String) changes.get('email') ?: this.event.email,
            (Map<String, Object>) (changes.get('payload') ?: this.event.payload),
            (UserDomainEventType) changes.get('type') ?: this.event.type,
            (UUID) changes.get('operationId') ?: this.event.operationId,
            (Long) changes.get('dateCreated') ?: this.event.dateCreated
        )
    }
}
