package com.rest_service.messaging.user.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
@Named("MessageUpdateInitiatedEventHandler_userDomain")
class MessageUpdateInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val userStateManager: UserStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun checkOperationFailed(operationId: UUID) = userStateManager.checkOperationFailed(operationId)

    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        return userStateManager.rebuildUser(event.responsibleUserId!!, event)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return userStateManager.mapDomainEvent(event.responsibleUserId!!, null, UserDomainEventType.MESSAGE_UPDATE_APPROVED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return userStateManager.saveEvent(event)
            .thenReturn(true)
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return userStateManager.handleError(event, error, SagaEventType.MESSAGE_UPDATE_REJECTED)
    }
}
