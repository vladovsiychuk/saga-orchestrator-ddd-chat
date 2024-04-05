package com.rest_service.messaging.message.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
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
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("RoomCreateInitiatedEventHandler_roomDomain")
class MessageCreateInitiatedEventHandler(
    private val repository: MessageDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()
    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        val operationId = event.operationId

        return repository.existsByOperationIdAndType(operationId, MessageDomainEventType.UNDO)
            .flatMap { operationFailed ->
                if (operationFailed)
                    return@flatMap Mono.empty()

                MessageDomain(operationId, event.responsibleUserEmail, event.responsibleUserId!!).toMono()
            }
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return MessageDomainEvent(
            messageId = UUID.randomUUID(),
            payload = mapper.convertValue(event.payload),
            type = MessageDomainEventType.MESSAGE_CREATED,
            operationId = event.operationId,
            responsibleUserId = event.responsibleUserId!!
        )
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return repository.save(event as MessageDomainEvent)
            .map { true }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return repository.existsByOperationIdAndType(event.operationId, MessageDomainEventType.UNDO)
            .flatMap { exists ->
                if (exists)
                    return@flatMap Mono.empty<Void>()

                val errorEvent = SagaEvent(
                    SagaEventType.MESSAGE_CREATE_REJECTED,
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
