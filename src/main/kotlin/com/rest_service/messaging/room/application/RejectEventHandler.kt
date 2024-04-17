package com.rest_service.messaging.room.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventRepository
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
@Named("RejectEventHandler_roomDomain")
class RejectEventHandler(private val repository: RoomDomainEventRepository) {
    private val mapper = jacksonObjectMapper()

    fun handleEvent(sagaEvent: SagaEvent) {
        repository.existsByOperationIdAndType(sagaEvent.operationId, RoomDomainEventType.UNDO)
            .flatMap { exists ->
                if (exists)
                    return@flatMap Mono.empty()

                createDomainEvent(sagaEvent)
                    .flatMap { repository.save(it) }
            }
            .subscribe()
    }

    private fun createDomainEvent(sagaEvent: SagaEvent): Mono<RoomDomainEvent> {
        return RoomDomainEvent(
            roomId = UUID.fromString("00000000-0000-0000-0000-000000000000"),
            payload = mapper.convertValue(sagaEvent.payload),
            type = RoomDomainEventType.UNDO,
            operationId = sagaEvent.operationId,
            responsibleUserId = sagaEvent.responsibleUserId,
        ).toMono()
    }
}
