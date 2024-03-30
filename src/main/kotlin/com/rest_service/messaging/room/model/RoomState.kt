package com.rest_service.messaging.room.model

import com.rest_service.commons.SagaEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import reactor.core.publisher.Mono

interface RoomState {
    fun apply(event: RoomDomainEvent): RoomDomainEvent
    fun createResponseEvent(): Mono<SagaEvent>
}
