package com.rest_service.messaging.room.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventRepository
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import com.rest_service.messaging.room.model.RoomDomain
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
class RoomStateManager(
    private val repository: RoomDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) {
    private val mapper = jacksonObjectMapper()
    fun checkOperationFailed(operationId: UUID): Mono<Boolean> {
        return repository.existsByOperationIdAndType(operationId, RoomDomainEventType.UNDO)
            .flatMap {
                if (it)
                    return@flatMap Mono.empty()
                else
                    true.toMono()
            }
    }

    fun rebuildRoom(roomId: UUID, operationId: UUID): Mono<Domain> {
        return repository.findDomainEvents(roomId)
            .collectList()
            .flatMap { events ->
                val roomDomain = RoomDomain(operationId)

                events.toFlux()
                    .takeUntil { event -> event.operationId == operationId }
                    .concatMap { event -> roomDomain.apply(event).toMono().thenReturn(roomDomain) }
                    .last()
                    .defaultIfEmpty(roomDomain)
            }
    }

    fun mapDomainEvent(roomId: UUID, type: RoomDomainEventType, event: SagaEvent): DomainEvent {
        return RoomDomainEvent(
            roomId = roomId,
            payload = mapper.convertValue(event.payload),
            type = type,
            operationId = event.operationId,
            responsibleUserId = event.responsibleUserId
        )
    }

    fun saveEvent(event: DomainEvent): Mono<RoomDomainEvent> {
        return repository.save(event as RoomDomainEvent)
    }

    fun handleError(event: SagaEvent, error: Throwable, type: SagaEventType): Mono<Void> {
        return checkOperationFailed(event.operationId)
            .flatMap {
                val errorEvent = SagaEvent(
                    type,
                    event.operationId,
                    ServiceEnum.ROOM_SERVICE,
                    event.responsibleUserId,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }
}
