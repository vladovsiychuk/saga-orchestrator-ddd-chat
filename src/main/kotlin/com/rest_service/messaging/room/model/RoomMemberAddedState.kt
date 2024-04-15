package com.rest_service.messaging.room.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import reactor.kotlin.core.publisher.toMono

class RoomMemberAddedState(private val domain: RoomDomain) : RoomState {
    constructor(domain: RoomDomain, event: RoomDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, RoomAddMemberCommand::class.java)
        domain.room!!.members.add(command.memberId)
    }

    private val mapper = jacksonObjectMapper()

    override fun createResponseEvent() = SagaEvent(SagaEventType.ROOM_ADD_MEMBER_APPROVED, domain.operationId, ServiceEnum.ROOM_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId, domain.room!!).toMono()
    override fun apply(event: RoomDomainEvent) = run {
        RoomCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
