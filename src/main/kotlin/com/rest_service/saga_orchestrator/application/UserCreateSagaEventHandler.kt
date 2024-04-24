package com.rest_service.saga_orchestrator.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.UserCreateSaga
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import reactor.core.publisher.Mono

@Singleton
class UserCreateSagaEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val sagaStateManager: SagaStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun rebuildDomainFromEvent(domainEvent: DomainEvent): Mono<Domain> {
        domainEvent as SagaDomainEvent
        val saga = UserCreateSaga(domainEvent.operationId, domainEvent.responsibleUserId)
        return sagaStateManager.rebuildSaga(domainEvent, saga)
    }

    override fun mapDomainEvent(): Mono<DomainEvent> {
        TODO("Not yet implemented")
    }

    override fun saveEvent(domainEvent: DomainEvent): Mono<DomainEvent> {
        return sagaStateManager.saveEvent(domainEvent).map { it }
    }

    override fun handleError(error: Throwable): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun createResponseSagaEvent(domain: Domain): Mono<SagaEvent> {
        TODO("Not yet implemented")
    }
}
