package com.rest_service.messaging.user.model

import com.rest_service.commons.SagaEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import reactor.core.publisher.Mono

interface UserState {
    fun apply(event: UserDomainEvent): UserDomainEvent
    fun createResponseEvent(sagaEvent: SagaEvent): Mono<SagaEvent>
}
