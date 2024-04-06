package com.rest_service.messaging.user.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.room.model.RoomDomain
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventRepository
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
class UserStateManager(
    private val repository: UserDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) {
    private val mapper = jacksonObjectMapper()
    fun checkOperationFailed(operationId: UUID): Mono<Boolean> {
        return repository.existsByOperationIdAndType(operationId, UserDomainEventType.UNDO)
            .flatMap {
                if (it)
                    return@flatMap Mono.empty()
                else
                    true.toMono()
            }
    }

    fun rebuildUser(roomId: UUID, event: SagaEvent): Mono<Domain> {
        return repository.findDomainEvents(roomId)
            .collectList()
            .flatMap { events ->
                val roomDomain = RoomDomain(event.operationId, event.responsibleUserEmail, event.responsibleUserId!!)

                if (events.isEmpty())
                    return@flatMap roomDomain.toMono()

                events.toFlux()
                    .concatMap { event ->
                        roomDomain.apply(event).toMono().thenReturn(roomDomain)
                    }
                    .last()
            }
    }

    fun mapDomainEvent(userId: UUID, email: String?, type: UserDomainEventType, event: SagaEvent): DomainEvent {
        return UserDomainEvent(
            userId = userId,
            email = email,
            payload = mapper.convertValue(event.payload),
            type = type,
            operationId = event.operationId,
        )
    }

    fun saveEvent(event: DomainEvent): Mono<UserDomainEvent> {
        return repository.save(event as UserDomainEvent)
    }

    fun handleError(event: SagaEvent, error: Throwable, type: SagaEventType): Mono<Void> {
        return checkOperationFailed(event.operationId)
            .flatMap {
                val errorEvent = SagaEvent(
                    type,
                    event.operationId,
                    ServiceEnum.USER_SERVICE,
                    event.responsibleUserEmail,
                    event.responsibleUserId!!,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }
}
