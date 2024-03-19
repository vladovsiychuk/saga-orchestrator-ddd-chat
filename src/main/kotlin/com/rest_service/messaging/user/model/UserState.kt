package com.rest_service.messaging.user.model

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.State
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import java.util.UUID
import reactor.core.publisher.Mono

class UserState(
    private val operationId: UUID,
    private val eventFactory: EventFactory
) : State {
    override fun apply(event: SagaEvent): Mono<Boolean> {
        TODO("Not yet implemented")
    }

    override fun createNextEvent(): Mono<DomainEvent> {
        TODO("Not yet implemented")
    }

}
