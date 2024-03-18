package com.rest_service.commons

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.model.SagaState
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

abstract class AbstractSagaEventHandler(
    private val applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
    securityManager: SecurityManager
) {
    private val mapper = jacksonObjectMapper()
    private val currentUser = securityManager.getUserEmail()

    protected abstract fun shouldHandle(sagaType: SagaType): Boolean
    protected abstract fun createNewState(operationId: UUID): SagaState
    protected abstract fun getRejectSagaType(): SagaType
    protected abstract fun saveEvent(newEvent: SagaEvent): Mono<SagaEvent>
    protected abstract fun findSagaEventsByOperationId(operationId: UUID): Flux<SagaEvent>
    protected abstract fun findRejectedEvent(operationId: UUID): Mono<SagaEvent>
    protected abstract fun getServiceName(): ServiceEnum

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
                            .flatMap { saveEvent(newEvent) }
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
        return findSagaEventsByOperationId(operationId)
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
        return findRejectedEvent(event.operationId)
            .switchIfEmpty {
                val errorEvent = DomainEvent(
                    getRejectSagaType(),
                    event.operationId,
                    getServiceName(),
                    currentUser,
                    mapOf("message" to error.message)
                )

                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
            .then()
    }
}
