package com.rest_service.messaging.room.model

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import reactor.kotlin.core.publisher.toMono

class MessageTranslateApprovedState(private val domain: RoomDomain) : RoomState {
    constructor(domain: RoomDomain, event: RoomDomainEvent) : this(domain) {
        if (domain.validateCommands)
            if (event.responsibleUserId !in domain.room!!.members)
                throw RuntimeException("User with id ${event.responsibleUserId} is not a member of the room with id ${domain.room!!.id}")
    }

    override fun createResponseEvent() = SagaEvent(SagaEventType.MESSAGE_TRANSLATE_APPROVED, domain.operationId, ServiceEnum.ROOM_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId, domain.room!!).toMono()
    override fun apply(event: RoomDomainEvent) = run {
        RoomCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
