package com.rest_service.messaging.user.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.UserCreateCommand
import com.rest_service.commons.dto.UserDTO
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import java.util.UUID

class UserCreatedState(private val domain: UserDomain) : UserState {
    constructor(domain: UserDomain, event: UserDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, UserCreateCommand::class.java)

        if (domain.operationId == event.operationId)
            if (UUID.nameUUIDFromBytes(command.email.toByteArray()) != event.responsibleUserId)
                throw RuntimeException("Responsible user doesn't have permissions to create the user")

        domain.currentUser = UserDTO(command, event.userId, event.dateCreated)
    }

    private val mapper = jacksonObjectMapper()

    override fun apply(event: UserDomainEvent): UserDomainEvent {
        when (event.type) {
            UserDomainEventType.ROOM_CREATE_APPROVED       -> RoomCreateApprovedState(domain)
            UserDomainEventType.ROOM_ADD_MEMBER_APPROVED   -> RoomAddMemberApprovedState(domain)
            UserDomainEventType.MESSAGE_UPDATE_APPROVED    -> MessageUpdateApprovedState(domain)
            UserDomainEventType.MESSAGE_READ_APPROVED      -> MessageReadApprovedState(domain)
            UserDomainEventType.MESSAGE_TRANSLATE_APPROVED -> MessageTranslateApprovedState(domain, event)
            else                                           ->
                throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        }.let { domain.changeState(it) }

        return event
    }
}
