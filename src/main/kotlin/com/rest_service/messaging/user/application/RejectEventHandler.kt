package com.rest_service.messaging.user.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventRepository
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class RejectEventHandler(private val repository: UserDomainEventRepository) {
    private val mapper = jacksonObjectMapper()

    fun handleEvent(sagaEvent: SagaEvent) {
        repository.existsByOperationIdAndType(sagaEvent.operationId, UserDomainEventType.UNDO)
            .flatMap { exists ->
                if (exists)
                    return@flatMap Mono.empty()

                createDomainEvent(sagaEvent)
                    .flatMap { repository.save(it) }
            }
            .subscribe()
    }

    private fun createDomainEvent(sagaEvent: SagaEvent): Mono<UserDomainEvent> {
        return UserDomainEvent(
            userId = UUID.fromString("00000000-0000-0000-0000-000000000000"),
            payload = mapper.convertValue(sagaEvent.payload),
            type = UserDomainEventType.UNDO,
            operationId = sagaEvent.operationId,
            responsibleUserId = sagaEvent.responsibleUserId,
        ).toMono()
    }
}
