package com.saga_orchestrator_ddd_chat.saga_orchestrator.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.Domain
import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.dto.ErrorDTO
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaDomainEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaEventRepository
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.AbstractSagaStateManager
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

    fun rebuildSaga(event: SagaDomainEvent, domain: AbstractSagaStateManager<*, *>): Mono<Domain> {
        return repository.findByOperationIdOrderByDateCreated(event.operationId)
            .takeUntil { it.id == event.id }
            .collectList()
            .flatMap { events ->

                events.toFlux()
                    .takeUntil { it.id == event.id }
                    .concatMap { event ->
                        domain.apply(event).toMono().thenReturn(domain)
                    }
                    .last()
            }
    }

    fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return mapper.convertValue(event, SagaDomainEvent::class.java)
    }

    fun saveEvent(event: DomainEvent): Mono<SagaDomainEvent> {
        return repository.save(event as SagaDomainEvent)
    }

    fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return checkOperationFailed(event.operationId, event.type.rejectedEventType!!)
            .flatMap {
                val errorEvent = SagaEvent(
                    event.type.rejectedEventType,
                    event.operationId,
                    ServiceEnum.SAGA_SERVICE,
                    event.responsibleUserId,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }

    private fun checkOperationFailed(operationId: UUID, rejectType: SagaEventType): Mono<Boolean> {
        return repository.existsByOperationIdAndType(operationId, rejectType)
            .flatMap {
                if (it)
                    return@flatMap Mono.empty()
                else
                    true.toMono()
            }
    }
}
