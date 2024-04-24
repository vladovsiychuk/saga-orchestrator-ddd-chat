package com.rest_service.messaging.room.model

import com.rest_service.messaging.room.infrastructure.RoomDomainEvent

class MessageReadApprovedState(private val domain: RoomDomain) : RoomState {
    constructor(domain: RoomDomain, event: RoomDomainEvent) : this(domain) {
        if (domain.operationId == event.operationId)
            if (event.responsibleUserId !in domain.room.members)
                throw RuntimeException("User with id ${event.responsibleUserId} is not a member of the room with id ${domain.room.id}")
    }

    override fun apply(event: RoomDomainEvent) = run {
        RoomCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
