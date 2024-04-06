package com.rest_service.messaging.room.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import com.rest_service.messaging.room.model.RoomDomain
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("RoomCreateInitiatedEventHandler_roomDomain")
class RoomCreateInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val roomStateManager: RoomStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun checkOperationFailed(operationId: UUID) = roomStateManager.checkOperationFailed(operationId)

    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        return RoomDomain(event.operationId, event.responsibleUserEmail, event.responsibleUserId!!).toMono()
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return roomStateManager.mapDomainEvent(UUID.randomUUID(), RoomDomainEventType.ROOM_CREATED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return roomStateManager.saveEvent(event)
            .thenReturn(true)
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return roomStateManager.handleError(event, error, SagaEventType.ROOM_CREATE_REJECTED)
    }
}
