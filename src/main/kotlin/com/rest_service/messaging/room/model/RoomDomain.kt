package com.rest_service.messaging.room.model

import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.dto.DTO
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import java.util.UUID

class RoomDomain(var operationId: UUID) : Domain {
    private var state: RoomState = RoomInCreationState(this)
    lateinit var room: RoomDTO

    fun apply(event: RoomDomainEvent): DomainEvent {
        return state.apply(event)
    }

    fun changeState(newState: RoomState) {
        state = newState
    }

    override fun toDto(): DTO {
        return room
    }
}
