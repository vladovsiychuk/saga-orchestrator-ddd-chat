package com.rest_service.saga_orchestrator.model

import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent

class SagaEventDSL {
    SagaEvent event = new SagaEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        [:],
        ServiceEnum.SAGA_SERVICE,
        "test-user",
        SagaType.ROOM_CREATE_START,
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

    SagaEventDSL ofType(SagaType type) {
        event.type = type
        return this
    }

    SagaEventDSL withAnyValidRoomCommand() {
        event.payload = ["userId": UUID.randomUUID()]
        return this
    }

    SagaEventDSL withAnyValidRoomDTO() {
        event.payload = [
            "id"         : UUID.randomUUID(),
            "createdBy"  : UUID.randomUUID(),
            "members"    : [],
            "dateCreated": 123,
            "dateUpdated": 123
        ]
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
