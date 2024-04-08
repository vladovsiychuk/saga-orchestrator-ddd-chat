package com.rest_service.saga_orchestrator.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.saga_orchestrator.model.MessageTranslateSaga
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class MessageTranslateSagaEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val sagaStateManager: SagaStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        val messageSaga = MessageTranslateSaga(event.operationId, event.responsibleUserEmail, event.responsibleUserId!!)
        return sagaStateManager.rebuildSaga(event.operationId, messageSaga)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent = sagaStateManager.mapDomainEvent(event)

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return sagaStateManager.saveEvent(event)
            .map { true }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return sagaStateManager.handleError(event, error, SagaEventType.MESSAGE_TRANSLATE_REJECTED)
    }

    override fun checkOperationFailed(operationId: UUID): Mono<Boolean> {
        return false.toMono()
    }
}
