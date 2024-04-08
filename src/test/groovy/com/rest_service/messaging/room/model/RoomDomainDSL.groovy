package com.rest_service.messaging.room.model

import com.rest_service.commons.SagaEvent

class RoomDomainDSL {
    RoomDomain domain = new RoomDomain(UUID.randomUUID(), "example@test.com", UUID.randomUUID())

    static RoomDomainDSL aRoom() {
        return new RoomDomainDSL()
    }

    static RoomDomainDSL the(RoomDomainDSL dsl) {
        return dsl
    }

    RoomDomainDSL and() {
        return this
    }

    RoomDomainDSL reactsTo(RoomDomainEventDSL event) {
        domain.apply(event.event)
        return this
    }

    RoomDomainDSL withOperationId(UUID operationId) {
        domain.operationId = operationId
        return this
    }

    RoomDomainDSL withResponsibleUserEmail(String responsibleUserEmail) {
        domain.responsibleUserEmail = responsibleUserEmail
        return this
    }

    RoomDomainDSL withResponsibleUserId(UUID userId) {
        domain.responsibleUserId = userId
        return this
    }

    SagaEvent responseEvent() {
        return domain.createResponseSagaEvent().block()
    }
}

