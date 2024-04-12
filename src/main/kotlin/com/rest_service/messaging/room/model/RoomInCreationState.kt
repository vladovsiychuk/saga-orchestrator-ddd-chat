package com.rest_service.messaging.room.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType

class RoomInCreationState(private val domain: RoomDomain) : RoomState {
    private val mapper = jacksonObjectMapper()

    override fun apply(event: RoomDomainEvent): RoomDomainEvent {
        return when (event.type) {
            RoomDomainEventType.ROOM_CREATED -> createRoom(event)
            else                             ->
                throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        }
    }

    private fun createRoom(event: RoomDomainEvent): RoomDomainEvent {
        val command = mapper.convertValue(event.payload, RoomCreateCommand::class.java)

        domain.room = RoomDTO(command, event)
        domain.changeState(RoomCreatedState(domain))
        return event
    }

    override fun createResponseEvent() = throw UnsupportedOperationException("No next event for room in creation state.")
}
