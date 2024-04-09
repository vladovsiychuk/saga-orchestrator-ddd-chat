package com.rest_service.messaging.message.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.MessageTranslateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
@Named("MessageReadInitiatedEventHandler_messageDomain")
class MessageTranslateInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val messageStateManager: MessageStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()
    override fun checkOperationFailed(operationId: UUID) = messageStateManager.checkOperationFailed(operationId)

    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)
        return messageStateManager.rebuildMessage(command.messageId, event)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)
        return messageStateManager.mapDomainEvent(command.messageId, MessageDomainEventType.MESSAGE_TRANSLATED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return messageStateManager.saveEvent(event)
            .thenReturn(true)
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return messageStateManager.handleError(event, error, SagaEventType.MESSAGE_TRANSLATE_REJECTED)
    }
}
