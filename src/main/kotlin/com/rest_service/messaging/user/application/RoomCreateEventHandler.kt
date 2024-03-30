package com.rest_service.messaging.user.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventRepository
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import com.rest_service.messaging.user.model.UserDomain
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
open class RoomCreateEventHandler(
    private val repository: UserDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()
    override fun shouldHandle(sagaEventType: SagaEventType) = sagaEventType == SagaEventType.ROOM_CREATE_INITIATE

    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        val command = mapper.convertValue(event, RoomCreateCommand::class.java)
        val operationId = event.operationId

        return repository.existsByOperationIdAndType(operationId, UserDomainEventType.UNDO)
            .flatMap { operationFailed ->
                if (operationFailed)
                    return@flatMap Mono.empty()

                rebuildUserById(command.companionId, operationId, event.responsibleUserEmail, event.responsibleUserId!!)
            }
    }

    private fun rebuildUserById(userId: UUID, operationId: UUID, responsibleUserEmail: String, responsibleUserId: UUID): Mono<Domain> {
        return repository.findDomainEvents(userId)
            .collectList()
            .flatMap { events ->
                val userDomain = UserDomain(responsibleUserEmail, responsibleUserId, operationId)

                if (events.isEmpty())
                    return@flatMap userDomain.toMono()

                events.toFlux()
                    .concatMap { event ->
                        userDomain.apply(event).toMono().thenReturn(userDomain)
                    }
                    .last()
            }
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        val command = mapper.convertValue(event, RoomCreateCommand::class.java)

        return UserDomainEvent(
            userId = command.companionId,
            payload = mapper.convertValue(event.payload),
            type = UserDomainEventType.ROOM_CREATE_APPROVED,
            operationId = event.operationId,
        )
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return repository.save(event as UserDomainEvent)
            .map { true }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return repository.existsByOperationIdAndType(event.operationId, UserDomainEventType.UNDO)
            .flatMap { exists ->
                if (exists)
                    return@flatMap Mono.empty<Void>()

                val errorEvent = SagaEvent(
                    SagaEventType.ROOM_CREATE_REJECT,
                    event.operationId,
                    event.responsibleService,
                    event.responsibleUserEmail,
                    event.responsibleUserId!!,
                    mapOf("message" to error.message)
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }
}
