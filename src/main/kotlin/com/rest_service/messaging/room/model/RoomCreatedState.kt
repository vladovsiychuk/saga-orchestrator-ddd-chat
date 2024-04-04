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
            RoomDomainEventType.ROOM_MEMBER_ADDED -> addMember(event)
            else                                  -> throw UnsupportedOperationException()
        }
    }

    private fun addMember(event: RoomDomainEvent): RoomDomainEvent {
        val command = mapper.convertValue(event.payload, RoomAddMemberCommand::class.java)
        domain.room!!.members.add(command.memberId)
        domain.changeState(RoomMemberAddedState(domain))
        return event

    }

    override fun createResponseEvent() = SagaEvent(SagaEventType.ROOM_CREATE_APPROVED, domain.operationId, ServiceEnum.USER_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId, domain.room!!).toMono()
}
