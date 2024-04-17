package com.rest_service.saga_orchestrator.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
class SagaStateManager(
    private val repository: SagaEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) {
    private val mapper = jacksonObjectMapper()
    fun checkOperationFailed(operationId: UUID, rejectType: SagaEventType): Mono<Boolean> {
        return repository.existsByOperationIdAndType(operationId, rejectType)
            .flatMap {
                if (it)
                    return@flatMap Mono.empty()
                else
                    true.toMono()
            }
    }

    fun rebuildSaga(event: SagaDomainEvent, domain: Domain): Mono<Domain> {
        return repository.findByOperationIdOrderByDateCreated(event.operationId)
            .collectList()
            .flatMap { events ->

                events.toFlux()
                    .takeUntil { it.id == event.id }
                    .concatMap { event ->
                        domain.apply(event).toMono().thenReturn(domain)
                    }
                    .last()
                    .defaultIfEmpty(domain)
            }
    }

    fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return mapper.convertValue(event, SagaDomainEvent::class.java)
    }

    fun saveEvent(event: DomainEvent): Mono<SagaDomainEvent> {
        return repository.save(event as SagaDomainEvent)
    }

    fun handleError(event: SagaEvent, error: Throwable, type: SagaEventType): Mono<Void> {
        return checkOperationFailed(event.operationId, type)
            .flatMap {
                val errorEvent = SagaEvent(
                    type,
                    event.operationId,
                    ServiceEnum.SAGA_SERVICE,
                    event.responsibleUserId,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }
}
