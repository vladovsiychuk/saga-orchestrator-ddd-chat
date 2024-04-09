package com.rest_service.messaging.room.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import reactor.kotlin.core.publisher.toMono

class RoomCreatedState(private val domain: RoomDomain) : RoomState {
    private val mapper = jacksonObjectMapper()

    override fun apply(event: RoomDomainEvent): RoomDomainEvent {
        return when (event.type) {
            RoomDomainEventType.ROOM_MEMBER_ADDED          -> addMember(event)
            RoomDomainEventType.MESSAGE_CREATE_APPROVED    -> approveMessageCreate(event)
            RoomDomainEventType.MESSAGE_READ_APPROVED      -> approveMessageRead(event)
            RoomDomainEventType.MESSAGE_TRANSLATE_APPROVED -> approveMessageTranslate(event)
            else                                           -> throw UnsupportedOperationException()
        }
    }

    private fun approveMessageTranslate(event: RoomDomainEvent): RoomDomainEvent {
        if (event.responsibleUserId !in domain.room!!.members)
            throw RuntimeException("User with id ${event.responsibleUserId} is not a member of the room with id ${domain.room!!.id}")

        domain.changeState(MessageTranslateApprovedState(domain))
        return event
    }

    private fun approveMessageRead(event: RoomDomainEvent): RoomDomainEvent {
        if (event.responsibleUserId !in domain.room!!.members)
            throw RuntimeException("User with id ${event.responsibleUserId} is not a member of the room with id ${domain.room!!.id}")

        domain.changeState(MessageReadApprovedState(domain))
        return event
    }

    private fun approveMessageCreate(event: RoomDomainEvent): RoomDomainEvent {
        domain.changeState(MessageCreateApprovedState(domain))
        return event
    }

    private fun addMember(event: RoomDomainEvent): RoomDomainEvent {
        val command = mapper.convertValue(event.payload, RoomAddMemberCommand::class.java)
        domain.room!!.members.add(command.memberId)
        domain.changeState(RoomMemberAddedState(domain))
        return event

    }

    override fun createResponseEvent() = SagaEvent(SagaEventType.ROOM_CREATE_APPROVED, domain.operationId, ServiceEnum.USER_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId, domain.room!!).toMono()
}
