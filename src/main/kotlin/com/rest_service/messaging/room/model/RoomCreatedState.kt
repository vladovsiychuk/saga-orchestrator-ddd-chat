package com.rest_service.messaging.room.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType

class RoomCreatedState(private val domain: RoomDomain) : RoomState {
    constructor(domain: RoomDomain, event: RoomDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, RoomCreateCommand::class.java)

        domain.room = RoomDTO(command, event)
    }

    private val mapper = jacksonObjectMapper()

    override fun apply(event: RoomDomainEvent): RoomDomainEvent {
        when (event.type) {
            RoomDomainEventType.ROOM_MEMBER_ADDED          -> RoomMemberAddedState(domain, event)
            RoomDomainEventType.MESSAGE_CREATE_APPROVED    -> MessageCreateApprovedState(domain)
            RoomDomainEventType.MESSAGE_READ_APPROVED      -> MessageReadApprovedState(domain, event)
            RoomDomainEventType.MESSAGE_TRANSLATE_APPROVED -> MessageTranslateApprovedState(domain, event)
            else                                           ->
                throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        }.let { domain.changeState(it) }

        return event
    }
}
