package com.rest_service.messaging.room.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import reactor.kotlin.core.publisher.toMono

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

    override fun createResponseEvent(sagaEvent: SagaEvent) = SagaEvent(SagaEventType.ROOM_CREATE_APPROVED, domain.operationId, ServiceEnum.ROOM_SERVICE, sagaEvent.responsibleUserId, domain.room).toMono()
}
