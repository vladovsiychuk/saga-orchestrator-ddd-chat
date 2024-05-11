package com.saga_orchestrator_ddd_chat.messaging.room.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.dto.ErrorDTO
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEvent
import com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEventRepository
import com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEventType
import com.saga_orchestrator_ddd_chat.messaging.room.model.Room
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class RoomStateManager(
    private val repository: RoomDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) {
    private val mapper = jacksonObjectMapper()

    fun rebuildRoom(roomId: UUID, operationId: UUID): Mono<Room> {
        return repository.findDomainEvents(roomId)
            .takeUntil { it.operationId == operationId }
            .reduce(Room()) { domain, event ->
                domain.apply(event)
                domain
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

    fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return checkOperationFailed(event.operationId)
            .flatMap {
                val errorEvent = SagaEvent(
                    event.type.rejectedEventType!!,
                    event.operationId,
                    ServiceEnum.ROOM_SERVICE,
                    event.responsibleUserId,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }

    private fun checkOperationFailed(operationId: UUID): Mono<Boolean> {
        return repository.existsByOperationIdAndType(operationId, RoomDomainEventType.UNDO)
            .flatMap {
                if (it)
                    return@flatMap Mono.empty()
                else
                    true.toMono()
            }
    }
}
