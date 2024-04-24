package com.rest_service.messaging.room.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent

class RoomMemberAddedState(private val domain: RoomDomain) : RoomState {
    constructor(domain: RoomDomain, event: RoomDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, RoomAddMemberCommand::class.java)
        domain.room.members.add(command.memberId)
    }

    private val mapper = jacksonObjectMapper()
    override fun apply(event: RoomDomainEvent) = run {
        RoomCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
