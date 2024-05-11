package com.saga_orchestrator_ddd_chat.messaging.room.model

import com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEvent
import com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEventType

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
        event = copyWith(type: type)
        return this
    }

    RoomDomainEventDSL withPayload(Map payload) {
        event = copyWith(payload: payload)
        return this
    }

    RoomDomainEventDSL from(UUID userId) {
        event = copyWith(responsibleUserId: userId)
        return this
    }

    private RoomDomainEvent copyWith(Map changes) {
        new RoomDomainEvent(
            (UUID) changes.get('eventId') ?: this.event.eventId,
            (UUID) changes.get('roomId') ?: this.event.roomId,
            (Map<String, Object>) (changes.get('payload') ?: this.event.payload),
            (RoomDomainEventType) changes.get('type') ?: this.event.type,
            (UUID) changes.get('responsibleUserId') ?: this.event.responsibleUserId,
            (UUID) changes.get('operationId') ?: this.event.operationId,
            (Long) changes.get('dateCreated') ?: this.event.dateCreated
        )
    }
}

