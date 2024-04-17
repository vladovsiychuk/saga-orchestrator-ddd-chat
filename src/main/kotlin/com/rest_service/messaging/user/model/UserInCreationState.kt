package com.rest_service.messaging.user.model

import com.rest_service.commons.SagaEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType

class UserInCreationState(private val domain: UserDomain) : UserState {
    override fun apply(event: UserDomainEvent): UserDomainEvent {
        return when (event.type) {
            UserDomainEventType.USER_CREATED -> domain.changeState(UserCreatedState(domain, event)).let { event }

            else                             ->
                throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        }
    }

    override fun createResponseEvent(sagaEvent: SagaEvent) = throw UnsupportedOperationException("No next event for user in creation state.")
}
