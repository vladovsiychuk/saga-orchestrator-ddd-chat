package com.rest_service.messaging.room.model

import com.rest_service.messaging.room.infrastructure.RoomDomainEvent

class MessageCreateApprovedState(private val domain: RoomDomain) : RoomState {
    override fun apply(event: RoomDomainEvent) = run {
        RoomCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
