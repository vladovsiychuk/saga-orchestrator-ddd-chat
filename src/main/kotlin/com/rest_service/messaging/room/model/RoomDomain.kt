package com.rest_service.messaging.room.model

import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import java.util.UUID
import reactor.core.publisher.Mono

class RoomDomain(var operationId: UUID) : Domain {
    private var state: RoomState = RoomInCreationState(this)
    lateinit var room: RoomDTO

    override fun apply(event: DomainEvent): DomainEvent {
        return state.apply(event as RoomDomainEvent)
    }

    override fun createResponseSagaEvent(sagaEvent: SagaEvent): Mono<SagaEvent> {
        return state.createResponseEvent(sagaEvent)
    }

    fun changeState(newState: RoomState) {
        state = newState
    }
}
