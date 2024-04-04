package com.rest_service.messaging.room.model

import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType

class RoomDomainEventDSL {
    RoomDomainEvent event = new RoomDomainEvent(
        UUID.randomUUID(),
        UUID.randomUUID(),
        [:],
        RoomDomainEventType.ROOM_CREATED,
        UUID.randomUUID(),
        UUID.randomUUID(),
        123123
    )

    static RoomDomainEventDSL anEvent() {
        return new RoomDomainEventDSL()
    }

    static RoomDomainEventDSL the(RoomDomainEventDSL dsl) {
        return dsl
    }

    RoomDomainEventDSL and() {
        return this
    }

    RoomDomainEventDSL ofType(RoomDomainEventType type) {
        event.type = type
        return this
    }

    RoomDomainEventDSL withPayload(Map payload) {
        event.payload = payload
        return this
    }

    RoomDomainEventDSL withOperationId(UUID operationId) {
        event.operationId = operationId
        return this
    }
}

