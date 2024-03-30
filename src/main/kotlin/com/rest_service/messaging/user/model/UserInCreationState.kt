package com.rest_service.messaging.user.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.UserCreateCommand
import com.rest_service.commons.dto.UserDTO
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType

class UserInCreationState(private val domain: UserDomain) : UserState {
    private val mapper = jacksonObjectMapper()

    override fun apply(event: UserDomainEvent): UserDomainEvent {
        return when (event.type) {
            UserDomainEventType.USER_CREATE -> createUser(event)
            else                            -> throw UnsupportedOperationException()
        }
    }

    private fun createUser(event: UserDomainEvent): UserDomainEvent {
        val command = mapper.convertValue(event.payload, UserCreateCommand::class.java)

        if (domain.responsibleUserEmail != command.email)
            throw RuntimeException("Responsible user doesn't have permissions to create the user")

        domain.currentUser = UserDTO(command, event.userId!!, event.dateCreated)
        domain.changeState(UserCreatedState(domain))
        return event
    }

    override fun createResponseEvent() = throw UnsupportedOperationException("No next event for user in creation state.")
}
