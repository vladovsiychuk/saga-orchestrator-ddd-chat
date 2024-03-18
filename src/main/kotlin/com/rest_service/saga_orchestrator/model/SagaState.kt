package com.rest_service.saga_orchestrator.model

import com.rest_service.commons.DomainEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import reactor.core.publisher.Mono

interface SagaState {
    fun apply(event: SagaEvent): Mono<Boolean>
    fun createNextEvent(): Mono<DomainEvent>
}
