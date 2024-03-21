package com.rest_service.messaging.user.model

import com.rest_service.commons.DomainEvent

class UserDomainDSL {
    UserDomain domain = new UserDomain("example@test.com", UUID.randomUUID())

    static UserDomainDSL aUser() {
        def dsl = new UserDomainDSL()
        dsl.from("example@test.com")
        return dsl
    }

    static UserDomainDSL the(UserDomainDSL dsl) {
        return dsl
    }

    UserDomainDSL and() {
        return this
    }

    UserDomainDSL reactsTo(UserDomainEventDSL event) {
        domain.apply(event.event)
        return this
    }

    UserDomainDSL from(String userEmail) {
        domain.responsibleUserEmail = userEmail
        return this
    }

    UserDomainDSL withOperationId(UUID operationId) {
        domain.operationId = operationId
        return this
    }

    DomainEvent nextEvent() {
        return domain.createNextEvent().block()
    }
}

