package com.rest_service.messaging.message.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
@Named("MessageCreateInitiatedEventHandler_messageDomain")
class MessageCreateInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val messageStateManager: MessageStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun rebuildDomainFromEvent(event: DomainEvent): Mono<Domain> {
        event as MessageDomainEvent
        return messageStateManager.rebuildMessage(event.messageId, event.operationId)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return messageStateManager.mapDomainEvent(UUID.randomUUID(), MessageDomainEventType.MESSAGE_CREATED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<DomainEvent> {
        return messageStateManager.saveEvent(event).map { it }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return messageStateManager.handleError(event, error, SagaEventType.MESSAGE_CREATE_REJECTED)
    }
}
