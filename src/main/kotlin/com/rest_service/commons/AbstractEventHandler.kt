package com.rest_service.commons

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.enums.EventType
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import com.rest_service.saga_orchestrator.model.SagaState
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

abstract class AbstractEventHandler(private val applicationEventPublisher: ApplicationEventPublisher<DomainEvent>) {
    private val mapper = jacksonObjectMapper()

    protected abstract fun shouldHandle(eventType: EventType): Boolean
    protected abstract fun createNewState(operationId: UUID): SagaState
    protected abstract fun saveEvent(newEvent: SagaEvent): Mono<SagaEvent>
    protected abstract fun findSagaEventsByOperationId(operationId: UUID): Flux<SagaEvent>
    protected abstract fun handleError(event: DomainEvent, error: Throwable): Mono<Void>

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
}
