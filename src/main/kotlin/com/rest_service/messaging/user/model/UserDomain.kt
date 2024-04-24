package com.rest_service.messaging.user.model

import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.dto.DTO
import com.rest_service.commons.dto.UserDTO
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import java.util.UUID

class UserDomain(val operationId: UUID) : Domain {
    private var state: UserState = UserInCreationState(this)
    lateinit var currentUser: UserDTO

    fun apply(event: UserDomainEvent): DomainEvent {
        return state.apply(event)
    }

    fun changeState(newState: UserState) {
        state = newState
    }

    override fun toDto(): DTO {
        return currentUser
    }
}
