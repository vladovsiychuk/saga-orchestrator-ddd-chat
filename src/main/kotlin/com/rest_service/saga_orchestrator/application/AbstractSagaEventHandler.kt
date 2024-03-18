package com.rest_service.saga_orchestrator.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.model.SagaState
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

abstract class AbstractSagaEventHandler(
    private val repository: SagaEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
    securityManager: SecurityManager
) {
    private val mapper = jacksonObjectMapper()
    private val currentUser = securityManager.getUserEmail()

    protected abstract fun shouldHandle(sagaType: SagaType): Boolean
    protected abstract fun createNewState(operationId: UUID): SagaState
    protected abstract fun getRejectSagaType(): SagaType

    @EventListener
    @Async
    open fun messageActionListener(event: DomainEvent) {
        if (shouldHandle(event.type))
            handleSagaEvent(event)
    }

    private fun handleSagaEvent(event: DomainEvent) {
        mapper.convertValue(event, SagaEvent::class.java)
            .let { newEvent ->
                rebuildSagaState(event.operationId)
                    .flatMap { sagaState ->
                        sagaState.apply(newEvent)
                            .flatMap { repository.save(newEvent) }
                            .map { sagaState }
                    }
            }
            .flatMap { it.createNextEvent() }
            .doOnNext { nextEvent -> applicationEventPublisher.publishEventAsync(nextEvent) }
            .then()
            .onErrorResume { handleError(event, it) }
            .subscribe()
    }

    private fun rebuildSagaState(operationId: UUID): Mono<SagaState> {
        return repository.findByOperationIdOrderByDateCreated(operationId)
            .collectList()
            .flatMap { events ->
                val sagaState = createNewState(operationId)

                if (events.isEmpty())
                    return@flatMap sagaState.toMono()

                events.toFlux()
                    .concatMap { event ->
                        sagaState.apply(event).thenReturn(sagaState)
                    }
                    .last()
            }
    }

    private fun handleError(event: DomainEvent, error: Throwable): Mono<Void> {
        return repository.findByOperationIdAndType(event.operationId, getRejectSagaType())
            .switchIfEmpty {
                val errorEvent = DomainEvent(
                    getRejectSagaType(),
                    event.operationId,
                    ServiceEnum.SAGA_SERVICE,
                    currentUser,
                    mapOf("message" to error.message)
                )

                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
            .then()
    }
}
