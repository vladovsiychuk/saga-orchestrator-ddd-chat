package com.rest_service.messaging.message.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventRepository
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType
import com.rest_service.messaging.message.model.MessageDomain
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
class MessageStateManager(
    private val repository: MessageDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) {
    private val mapper = jacksonObjectMapper()
    fun checkOperationFailed(operationId: UUID): Mono<Boolean> {
        return repository.existsByOperationIdAndType(operationId, MessageDomainEventType.UNDO)
            .flatMap {
                if (it)
                    return@flatMap Mono.empty()
                else
                    true.toMono()
            }
    }

    fun rebuildMessage(messageId: UUID, event: SagaEvent): Mono<Domain> {
        return repository.findDomainEvents(messageId)
            .collectList()
            .flatMap { events ->
                val roomDomain = MessageDomain(
                    event.operationId,
                    event.responsibleUserEmail,
                    event.responsibleUserId!!,
                )

                if (events.isEmpty())
                    return@flatMap roomDomain.toMono()

                events.toFlux()
                    .concatMap { event ->
                        roomDomain.apply(event).toMono().thenReturn(roomDomain)
                    }
                    .last()
            }
    }

    fun mapDomainEvent(roomId: UUID, type: MessageDomainEventType, event: SagaEvent): DomainEvent {
        return MessageDomainEvent(
            messageId = roomId,
            payload = mapper.convertValue(event.payload),
            type = type,
            operationId = event.operationId,
            responsibleUserId = event.responsibleUserId!!
        )
    }

    fun saveEvent(event: DomainEvent): Mono<MessageDomainEvent> {
        return repository.save(event as MessageDomainEvent)
    }

    fun handleError(event: SagaEvent, error: Throwable, type: SagaEventType): Mono<Void> {
        return checkOperationFailed(event.operationId)
            .flatMap {
                val errorEvent = SagaEvent(
                    type,
                    event.operationId,
                    ServiceEnum.MESSAGE_SERVICE,
                    event.responsibleUserEmail,
                    event.responsibleUserId!!,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }
}
