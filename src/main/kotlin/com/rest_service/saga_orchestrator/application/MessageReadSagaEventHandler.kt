package com.rest_service.saga_orchestrator.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.MessageReadSaga
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class MessageReadSagaEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val sagaStateManager: SagaStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun rebuildDomainFromEvent(event: DomainEvent): Mono<Domain> {
        event as SagaDomainEvent
        val messageSaga = MessageReadSaga(event.operationId, event.responsibleUserId)
        return sagaStateManager.rebuildSaga(event, messageSaga)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent = sagaStateManager.mapDomainEvent(event)

    override fun saveEvent(event: DomainEvent): Mono<DomainEvent> {
        return sagaStateManager.saveEvent(event).map { it }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return sagaStateManager.handleError(event, error, SagaEventType.MESSAGE_READ_REJECTED)
    }

    override fun checkOperationFailed(operationId: UUID): Mono<Boolean> {
        return false.toMono()
    }
}
