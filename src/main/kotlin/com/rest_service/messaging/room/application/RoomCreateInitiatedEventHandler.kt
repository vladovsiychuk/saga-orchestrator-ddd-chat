package com.rest_service.messaging.room.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
@Named("RoomCreateInitiatedEventHandler_roomDomain")
class RoomCreateInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val roomStateManager: RoomStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    override fun rebuildDomainFromEvent(event: DomainEvent): Mono<Domain> {
        event as RoomDomainEvent
        return roomStateManager.rebuildRoom(event.roomId, event.operationId)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        return roomStateManager.mapDomainEvent(UUID.randomUUID(), RoomDomainEventType.ROOM_CREATED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<DomainEvent> {
        return roomStateManager.saveEvent(event).map { it }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return roomStateManager.handleError(event, error, SagaEventType.ROOM_CREATE_REJECTED)
    }
}
