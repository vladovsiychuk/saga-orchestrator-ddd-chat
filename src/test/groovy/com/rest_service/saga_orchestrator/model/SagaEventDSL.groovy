package com.rest_service.saga_orchestrator.model

import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent

class SagaEventDSL {
    SagaDomainEvent event = new SagaDomainEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        [:],
        ServiceEnum.SAGA_SERVICE,
        "test-user",
        UUID.randomUUID(),
        SagaEventType.ROOM_CREATE_START,
        123123,
    )

    static SagaEventDSL anEvent() {
        return new SagaEventDSL()
    }

    static SagaEventDSL the(SagaEventDSL dsl) {
        return dsl
    }

    SagaEventDSL and() {
        return this
    }

    SagaEventDSL ofType(SagaEventType type) {
        event.type = type
        return this
    }

    SagaEventDSL with(Map payload) {
        event.payload = payload
        return this
    }

    SagaEventDSL withPayload(Map payload) {
        event.payload = payload
        return this
    }

    SagaEventDSL withOperationId(UUID operationId) {
        event.operationId = operationId
        return this
    }

    SagaEventDSL from(ServiceEnum service) {
        event.responsibleService = service
        return this
    }
}
