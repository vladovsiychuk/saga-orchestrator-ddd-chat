package com.rest_service.messaging.user.model

import com.rest_service.commons.SagaEvent

class UserDomainDSL {
    UserDomain domain = new UserDomain(UUID.randomUUID(), "example@test.com", UUID.randomUUID(), true)

    static UserDomainDSL aUser() {
        return new UserDomainDSL()
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

    UserDomainDSL withOperationId(UUID operationId) {
        domain.operationId = operationId
        return this
    }

    UserDomainDSL withResponsibleUserEmail(String responsibleUserEmail) {
        domain.responsibleUserEmail = responsibleUserEmail
        return this
    }

    SagaEvent responseEvent() {
        return domain.createResponseSagaEvent().block()
    }
}

