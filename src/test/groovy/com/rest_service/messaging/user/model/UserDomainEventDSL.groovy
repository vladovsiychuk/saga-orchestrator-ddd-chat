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
        UUID.randomUUID(),
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
        event.type = type
        return this
    }

    UserDomainEventDSL withPayload(Map payload) {
        event.payload = payload
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
}

