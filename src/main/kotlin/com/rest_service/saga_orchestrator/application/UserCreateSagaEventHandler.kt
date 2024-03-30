package com.rest_service.saga_orchestrator.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.model.UserCreateSaga
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("userCreateSagaEventHandler")
open class UserCreateSagaEventHandler(
    private val repository: SagaEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val securityManager: SecurityManager,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()

    private val currentUser = securityManager.getCurrentUserEmail()
    override fun shouldHandle(sagaEventType: SagaEventType): Boolean {
        return sagaEventType in listOf(SagaEventType.USER_CREATE_START, SagaEventType.USER_CREATE_APPROVE, SagaEventType.USER_CREATE_REJECT)
    }

    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        val operationId = event.operationId

        return repository.findByOperationIdOrderByDateCreated(operationId)
            .collectList()
            .flatMap { events ->
                val sagaState = UserCreateSaga(operationId, event.responsibleUserEmail)

                if (events.isEmpty())
                    return@flatMap sagaState.toMono()

                events.toFlux()
                    .concatMap { event ->
                        sagaState.apply(event).toMono().thenReturn(sagaState)
                    }
                    .last()
            }
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent = mapper.convertValue(event, SagaDomainEvent::class.java)
    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return repository.save(event as SagaDomainEvent)
            .map { true }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return repository.existsByOperationIdAndType(event.operationId, SagaEventType.USER_CREATE_REJECT)
            .flatMap { exists ->
                if (exists)
                    return@flatMap Mono.empty<Void>()

                val errorEvent = SagaEvent(
                    SagaEventType.USER_CREATE_REJECT,
                    event.operationId,
                    ServiceEnum.SAGA_SERVICE,
                    currentUser,
                    null,
                    mapOf("message" to error.message)
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }
}
