package com.rest_service.messaging.user.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.MessageTranslateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.commons.enums.UserType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import reactor.kotlin.core.publisher.toMono

class UserCreatedState(private val domain: UserDomain) : UserState {
    private val mapper = jacksonObjectMapper()

    override fun apply(event: UserDomainEvent): UserDomainEvent {
        return when (event.type) {
            UserDomainEventType.ROOM_CREATE_APPROVED       -> approveRoomCreate(event)
            UserDomainEventType.ROOM_ADD_MEMBER_APPROVED   -> approveRoomAddMember(event)
            UserDomainEventType.MESSAGE_UPDATE_APPROVED    -> approveMessageCreate(event)
            UserDomainEventType.MESSAGE_READ_APPROVED      -> approveMessageRead(event)
            UserDomainEventType.MESSAGE_TRANSLATE_APPROVED -> approveMessageTranslate(event)
            else                                           ->
                throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        }
    }

    private fun approveMessageTranslate(event: UserDomainEvent): UserDomainEvent {
        val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)
        val user = domain.currentUser!!

        if (user.type != UserType.TRANSLATOR)
            throw RuntimeException("User with id ${user.id} is not a translator.")
        else if (!user.translationLanguages!!.contains(command.language))
            throw RuntimeException("User with id ${user.id} cannot translate ${command.language}")

        domain.changeState(MessageTranslateApprovedState(domain))
        return event
    }

    private fun approveMessageRead(event: UserDomainEvent): UserDomainEvent {
        domain.changeState(MessageReadApprovedState(domain))
        return event
    }

    private fun approveMessageCreate(event: UserDomainEvent): UserDomainEvent {
        domain.changeState(MessageUpdateApprovedState(domain))
        return event
    }

    private fun approveRoomAddMember(event: UserDomainEvent): UserDomainEvent {
        domain.changeState(RoomAddMemberApprovedState(domain))
        return event
    }

    private fun approveRoomCreate(event: UserDomainEvent): UserDomainEvent {
        domain.changeState(RoomCreateApprovedState(domain))
        return event
    }

    override fun createResponseEvent() = SagaEvent(SagaEventType.USER_CREATE_APPROVED, domain.operationId, ServiceEnum.USER_SERVICE, domain.responsibleUserEmail, domain.currentUser!!.id, domain.currentUser!!).toMono()
}
