package com.rest_service.messaging.user.model

import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.UserDTO
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import java.util.UUID
import reactor.core.publisher.Mono

class UserDomain(
    var responsibleUserEmail: String,
    val responsibleUserId: UUID?,
    val operationId: UUID
) : Domain {
    private var state: UserState = UserInCreationState(this)
    var currentUser: UserDTO? = null

    override fun apply(event: DomainEvent): DomainEvent {
        return state.apply(event as UserDomainEvent)
    }

    override fun createResponseSagaEvent(): Mono<SagaEvent> {
        return state.createResponseEvent()
    }

    fun changeState(newState: UserState) {
        state = newState
    }
}
