package com.rest_service.messaging.room.model

import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType

class RoomInCreationState(private val domain: RoomDomain) : RoomState {
    override fun apply(event: RoomDomainEvent): RoomDomainEvent {
        when (event.type) {
            RoomDomainEventType.ROOM_CREATED -> RoomCreatedState(domain, event)
            else                             ->
                throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        }.let { domain.changeState(it) }

        return event
    }

    override fun createResponseEvent() = throw UnsupportedOperationException("No next event for room in creation state.")
}
