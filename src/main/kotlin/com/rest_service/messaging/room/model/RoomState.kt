package com.rest_service.messaging.room.model

import com.rest_service.messaging.room.infrastructure.RoomDomainEvent

interface RoomState {
    fun apply(event: RoomDomainEvent): RoomDomainEvent
}
