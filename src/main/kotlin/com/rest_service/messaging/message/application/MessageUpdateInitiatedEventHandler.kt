package com.rest_service.messaging.message.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType
import com.rest_service.messaging.message.model.MessageDomain
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("MessageUpdateInitiatedEventHandler_messageDomain")
class MessageUpdateInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val messageStateManager: MessageStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun checkOperationFailed(operationId: UUID) = messageStateManager.checkOperationFailed(operationId)

    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        return MessageDomain(event.operationId, event.responsibleUserEmail, event.responsibleUserId!!).toMono()
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return messageStateManager.mapDomainEvent(UUID.randomUUID(), MessageDomainEventType.MESSAGE_UPDATED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return messageStateManager.saveEvent(event)
            .thenReturn(true)
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return messageStateManager.handleError(event, error, SagaEventType.MESSAGE_UPDATE_REJECTED)
    }
}
