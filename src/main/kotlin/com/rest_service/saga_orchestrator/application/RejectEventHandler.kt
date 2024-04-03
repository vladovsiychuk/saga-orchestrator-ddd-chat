package com.rest_service.saga_orchestrator.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class RejectEventHandler(private val repository: SagaEventRepository) {
    private val mapper = jacksonObjectMapper()

    fun handleEvent(sagaEvent: SagaEvent) {
        repository.existsByOperationIdAndType(sagaEvent.operationId, sagaEvent.type)
            .flatMap { exists ->
                if (exists)
                    return@flatMap Mono.empty()

                createDomainEvent(sagaEvent)
                    .flatMap { repository.save(it) }
            }
            .subscribe()
    }

    private fun createDomainEvent(sagaEvent: SagaEvent): Mono<SagaDomainEvent> {
        return SagaDomainEvent(
            operationId = sagaEvent.operationId,
            payload = mapper.convertValue(sagaEvent.payload),
            responsibleService = sagaEvent.responsibleService,
            responsibleUserEmail = sagaEvent.responsibleUserEmail,
            type = sagaEvent.type,
        ).toMono()
    }
}
