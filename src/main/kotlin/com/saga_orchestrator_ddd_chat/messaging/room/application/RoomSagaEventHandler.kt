package com.saga_orchestrator_ddd_chat.messaging.room.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.AbstractEventHandler
import com.saga_orchestrator_ddd_chat.commons.Domain
import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.client.ViewServiceFetcher
import com.saga_orchestrator_ddd_chat.commons.command.MessageCreateCommand
import com.saga_orchestrator_ddd_chat.commons.command.MessageReadCommand
import com.saga_orchestrator_ddd_chat.commons.command.MessageTranslateCommand
import com.saga_orchestrator_ddd_chat.commons.command.RoomAddMemberCommand
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEvent
import com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class RoomSagaEventHandler(
    private val rejectEventHandler: RejectEventHandler,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val roomStateManager: RoomStateManager,
    private val viewServiceFetcher: ViewServiceFetcher,
) {
    private val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.ROOM_CREATE_INITIATED       -> handleEventWithMapper(event) {
                roomStateManager.mapDomainEvent(UUID.randomUUID(), RoomDomainEventType.ROOM_CREATED, event)
            }

            SagaEventType.ROOM_ADD_MEMBER_INITIATED   -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, RoomAddMemberCommand::class.java)
                roomStateManager.mapDomainEvent(command.roomId, RoomDomainEventType.ROOM_MEMBER_ADDED, event)
            }

            SagaEventType.MESSAGE_CREATE_INITIATED    -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, MessageCreateCommand::class.java)
                roomStateManager.mapDomainEvent(command.roomId, RoomDomainEventType.MESSAGE_CREATE_APPROVED, event)
            }

            SagaEventType.MESSAGE_READ_INITIATED      -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, MessageReadCommand::class.java)

                viewServiceFetcher.getMessage(command.messageId)
                    .map { message ->
                        roomStateManager.mapDomainEvent(message.roomId, RoomDomainEventType.MESSAGE_READ_APPROVED, event)
                    }
                    .block()!!
            }

            SagaEventType.MESSAGE_TRANSLATE_INITIATED -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)

                viewServiceFetcher.getMessage(command.messageId)
                    .map { message ->
                        roomStateManager.mapDomainEvent(message.roomId, RoomDomainEventType.MESSAGE_TRANSLATE_APPROVED, event)
                    }
                    .block()!!
            }

            SagaEventType.ROOM_CREATE_REJECTED,
            SagaEventType.ROOM_ADD_MEMBER_REJECTED,
            SagaEventType.MESSAGE_CREATE_REJECTED,
            SagaEventType.MESSAGE_READ_REJECTED,
            SagaEventType.MESSAGE_TRANSLATE_REJECTED  -> rejectEventHandler.handleEvent(event)

            else                                      -> {}
        }
    }

    private fun handleEventWithMapper(sagaEvent: SagaEvent, mapper: () -> DomainEvent) {
        object : AbstractEventHandler(applicationEventPublisher) {
            override fun rebuildDomainFromEvent(domainEvent: DomainEvent): Mono<Domain> {
                domainEvent as RoomDomainEvent
                return roomStateManager.rebuildRoom(domainEvent.roomId, domainEvent.operationId).map { it }
            }

            override fun mapDomainEvent() = mapper().toMono()

            override fun saveEvent(domainEvent: DomainEvent): Mono<DomainEvent> =
                roomStateManager.saveEvent(domainEvent).map { it }

            override fun handleError(error: Throwable) = roomStateManager.handleError(sagaEvent, error)

            override fun createResponseSagaEvent(domain: Domain) =
                SagaEvent(sagaEvent.type.approvedEventType!!, sagaEvent.operationId, ServiceEnum.ROOM_SERVICE, sagaEvent.responsibleUserId, domain.toDto()).toMono()

        }.handleEvent()
    }
}
