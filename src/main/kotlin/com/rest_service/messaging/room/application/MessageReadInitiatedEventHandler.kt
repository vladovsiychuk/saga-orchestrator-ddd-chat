package com.rest_service.messaging.room.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.client.ViewServiceFetcher
import com.rest_service.commons.command.MessageReadCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
@Named("MessageReadInitiatedEventHandler_roomDomain")
class MessageReadInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val roomStateManager: RoomStateManager,
    private val viewServiceFetcher: ViewServiceFetcher,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()
    override fun checkOperationFailed(operationId: UUID) = roomStateManager.checkOperationFailed(operationId)

    override fun rebuildDomain(event: SagaEvent): Mono<Domain> {
        val command = mapper.convertValue(event.payload, MessageReadCommand::class.java)
        return viewServiceFetcher.getMessage(command.messageId)
            .flatMap { message -> roomStateManager.rebuildRoom(message.roomId, event) }
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        val command = mapper.convertValue(event.payload, MessageReadCommand::class.java)

        return viewServiceFetcher.getMessage(command.messageId)
            .map { message ->
                roomStateManager.mapDomainEvent(message.roomId, RoomDomainEventType.MESSAGE_READ_APPROVED, event)
            }
            .block()!!
    }

    override fun saveEvent(event: DomainEvent): Mono<Boolean> {
        return roomStateManager.saveEvent(event)
            .thenReturn(true)
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return roomStateManager.handleError(event, error, SagaEventType.MESSAGE_READ_REJECTED)
    }
}
