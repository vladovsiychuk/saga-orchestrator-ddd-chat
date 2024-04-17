package com.rest_service.messaging.user.model

import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.UserDTO
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import java.util.UUID
import reactor.core.publisher.Mono

class UserDomain(val operationId: UUID) : Domain {
    private var state: UserState = UserInCreationState(this)
    lateinit var currentUser: UserDTO

    override fun apply(event: DomainEvent): DomainEvent {
        return state.apply(event as UserDomainEvent)
    }

    override fun createResponseSagaEvent(sagaEvent: SagaEvent): Mono<SagaEvent> {
        return state.createResponseEvent(sagaEvent)
    }

    fun changeState(newState: UserState) {
        state = newState
    }
}
