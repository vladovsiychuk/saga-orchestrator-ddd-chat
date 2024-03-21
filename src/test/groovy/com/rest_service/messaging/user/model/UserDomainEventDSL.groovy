package com.rest_service.messaging.user.model

import com.rest_service.commons.enums.EventType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent

class UserDomainEventDSL {
    UserDomainEvent event = new UserDomainEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "example@test.com",
        [:],
        EventType.USER_CREATE_INITIATE,
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

    UserDomainEventDSL ofType(EventType type) {
        event.type = type
        return this
    }

//    UserDomainEventDSL withUserId(UUID userId) {
//        event.userId = userId
//        return this
//    }
//
//    UserDomainEventDSL withEmail(String email) {
//        event.email = email
//        return this
//    }

    UserDomainEventDSL withPayload(Map payload) {
        event.payload = payload
        return this
    }

    UserDomainEventDSL withOperationId(UUID operationId) {
        event.operationId = operationId
        return this
    }

//    UserDomainEventDSL happenedAt(Long dateCreated) {
//        event.dateCreated = dateCreated
//        return this
//    }
}

