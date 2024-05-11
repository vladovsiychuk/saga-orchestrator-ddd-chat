package com.saga_orchestrator_ddd_chat.messaging.message.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.dto.ErrorDTO
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.messaging.message.infrastructure.MessageDomainEvent
import com.saga_orchestrator_ddd_chat.messaging.message.infrastructure.MessageDomainEventRepository
import com.saga_orchestrator_ddd_chat.messaging.message.infrastructure.MessageDomainEventType
import com.saga_orchestrator_ddd_chat.messaging.message.model.Message
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class MessageStateManager(
    private val repository: MessageDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) {
    private val mapper = jacksonObjectMapper()

    fun rebuildMessage(messageId: UUID, operationId: UUID): Mono<Message> {
        return repository.findDomainEvents(messageId)
            .takeUntil { it.operationId == operationId }
            .reduce(Message()) { domain, event ->
                domain.apply(event)
                domain
            }
    }

    fun mapDomainEvent(roomId: UUID, type: MessageDomainEventType, event: SagaEvent): DomainEvent {
        return MessageDomainEvent(
            messageId = roomId,
            payload = mapper.convertValue(event.payload),
            type = type,
            operationId = event.operationId,
            responsibleUserId = event.responsibleUserId
        )
    }

    fun saveEvent(event: DomainEvent): Mono<MessageDomainEvent> {
        return repository.save(event as MessageDomainEvent)
    }

    fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return checkOperationFailed(event.operationId)
            .flatMap {
                val errorEvent = SagaEvent(
                    event.type.rejectedEventType!!,
                    event.operationId,
                    ServiceEnum.MESSAGE_SERVICE,
                    event.responsibleUserId,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }

    private fun checkOperationFailed(operationId: UUID): Mono<Boolean> {
        return repository.existsByOperationIdAndType(operationId, MessageDomainEventType.UNDO)
            .flatMap {
                if (it)
                    return@flatMap Mono.empty()
                else
                    true.toMono()
            }
    }
}
