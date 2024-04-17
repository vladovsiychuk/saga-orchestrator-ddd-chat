package com.rest_service.messaging.message.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventRepository
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("RejectEventHandler_messageDomain")
class RejectEventHandler(private val repository: MessageDomainEventRepository) {
    private val mapper = jacksonObjectMapper()

    fun handleEvent(sagaEvent: SagaEvent) {
        repository.existsByOperationIdAndType(sagaEvent.operationId, MessageDomainEventType.UNDO)
            .flatMap { exists ->
                if (exists)
                    return@flatMap Mono.empty()

                createDomainEvent(sagaEvent)
                    .flatMap { repository.save(it) }
            }
            .subscribe()
    }

    private fun createDomainEvent(sagaEvent: SagaEvent): Mono<MessageDomainEvent> {
        return MessageDomainEvent(
            messageId = UUID.fromString("00000000-0000-0000-0000-000000000000"),
            payload = mapper.convertValue(sagaEvent.payload),
            type = MessageDomainEventType.UNDO,
            operationId = sagaEvent.operationId,
            responsibleUserId = sagaEvent.responsibleUserId,
        ).toMono()
    }
}
