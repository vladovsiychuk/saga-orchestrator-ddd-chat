package com.rest_service.messaging.user.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventRepository
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class RejectEventHandler(private val repository: UserDomainEventRepository) {

    private val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(sagaEvent: SagaEvent) {
        if (sagaEvent.type == SagaEventType.USER_CREATE_REJECT)
            handleEvent(sagaEvent)
    }

    private fun handleEvent(sagaEvent: SagaEvent) {
        createDomainEvent(sagaEvent)
            .flatMap { repository.save(it) }
            .subscribe()
    }

    private fun createDomainEvent(sagaEvent: SagaEvent): Mono<UserDomainEvent> {
        return UserDomainEvent(
            payload = mapper.convertValue(sagaEvent.payload),
            type = UserDomainEventType.UNDO,
            operationId = sagaEvent.operationId
        ).toMono()
    }
}
