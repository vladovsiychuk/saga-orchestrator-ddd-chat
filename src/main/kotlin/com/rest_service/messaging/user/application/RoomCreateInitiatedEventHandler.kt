package com.rest_service.messaging.user.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
@Named("roomCreateInitiatedEventHandler_userDomain")
class RoomCreateInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val userStateManager: UserStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()
    override fun checkOperationFailed(operationId: UUID) = userStateManager.checkOperationFailed(operationId)

    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        val command = mapper.convertValue(event.payload, RoomCreateCommand::class.java)
        return userStateManager.rebuildUser(command.companionId, event)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        val command = mapper.convertValue(event.payload, RoomCreateCommand::class.java)
        return userStateManager.mapDomainEvent(command.companionId, null, UserDomainEventType.ROOM_CREATE_APPROVED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return userStateManager.saveEvent(event)
            .thenReturn(true)
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return userStateManager.handleError(event, error, SagaEventType.ROOM_CREATE_REJECTED)
    }
}
