package com.rest_service.saga_orchestrator.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.State
import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.model.UserCreateSagaState
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("userCreateSagaEventHandler")
open class UserCreateSagaEventHandler(
    private val repository: SagaEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
    private val securityManager: SecurityManager,
    private val eventFactory: EventFactory,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()

    private val currentUser = securityManager.getUserEmail()
    override fun shouldHandle(eventType: EventType): Boolean {
        return eventType in listOf(EventType.USER_CREATE_START, EventType.USER_CREATE_APPROVE, EventType.USER_CREATE_REJECT)
    }

    override fun rebuildState(event: DomainEvent): Mono<State> {
        val operationId = event.operationId

        return repository.findByOperationIdOrderByDateCreated(operationId)
            .collectList()
            .flatMap { events ->
                val sagaState = UserCreateSagaState(operationId, eventFactory)

                if (events.isEmpty())
                    return@flatMap sagaState.toMono()

                events.toFlux()
                    .concatMap { event ->
                        sagaState.apply(event).thenReturn(sagaState)
                    }
                    .last()
            }
    }

    override fun saveEvent(newEvent: DomainEvent): Mono<Boolean> {
        return repository.save(mapper.convertValue(newEvent, SagaEvent::class.java))
            .map { true }
    }

    override fun handleError(event: DomainEvent, error: Throwable): Mono<Void> {
        return repository.findByOperationIdAndType(event.operationId, EventType.USER_CREATE_REJECT)
            .switchIfEmpty {
                val errorEvent = DomainEvent(
                    EventType.USER_CREATE_REJECT,
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
