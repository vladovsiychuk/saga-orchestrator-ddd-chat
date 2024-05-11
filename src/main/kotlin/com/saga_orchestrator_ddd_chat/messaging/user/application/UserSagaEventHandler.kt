package com.saga_orchestrator_ddd_chat.messaging.user.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.AbstractEventHandler
import com.saga_orchestrator_ddd_chat.commons.Domain
import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.command.RoomAddMemberCommand
import com.saga_orchestrator_ddd_chat.commons.command.RoomCreateCommand
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEvent
import com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class UserSagaEventHandler(
    private val rejectEventHandler: RejectEventHandler,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val userStateManager: UserStateManager,
) {
    private val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_INITIATED       -> handleEventWithMapper(event) {
                userStateManager.mapDomainEvent(event.responsibleUserId, UserDomainEventType.USER_CREATED, event)
            }

            SagaEventType.ROOM_CREATE_INITIATED       -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, RoomCreateCommand::class.java)
                userStateManager
                    .mapDomainEvent(command.companionId, UserDomainEventType.ROOM_CREATE_APPROVED, event)
            }

            SagaEventType.ROOM_ADD_MEMBER_INITIATED   -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, RoomAddMemberCommand::class.java)
                userStateManager
                    .mapDomainEvent(command.memberId, UserDomainEventType.ROOM_ADD_MEMBER_APPROVED, event)
            }

            SagaEventType.MESSAGE_UPDATE_INITIATED    -> handleEventWithMapper(event) {
                userStateManager.mapDomainEvent(event.responsibleUserId, UserDomainEventType.MESSAGE_UPDATE_APPROVED, event)
            }

            SagaEventType.MESSAGE_READ_INITIATED      -> handleEventWithMapper(event) {
                userStateManager.mapDomainEvent(event.responsibleUserId, UserDomainEventType.MESSAGE_READ_APPROVED, event)
            }

            SagaEventType.MESSAGE_TRANSLATE_INITIATED -> handleEventWithMapper(event) {
                userStateManager.mapDomainEvent(event.responsibleUserId, UserDomainEventType.MESSAGE_TRANSLATE_APPROVED, event)
            }

            SagaEventType.USER_CREATE_REJECTED,
            SagaEventType.ROOM_CREATE_REJECTED,
            SagaEventType.ROOM_ADD_MEMBER_REJECTED,
            SagaEventType.MESSAGE_UPDATE_REJECTED,
            SagaEventType.MESSAGE_READ_REJECTED,
            SagaEventType.MESSAGE_TRANSLATE_REJECTED  -> rejectEventHandler.handleEvent(event)

            else                                      -> {}
        }
    }

    private fun handleEventWithMapper(sagaEvent: SagaEvent, mapper: () -> DomainEvent) {
        object : AbstractEventHandler(applicationEventPublisher) {
            override fun rebuildDomainFromEvent(domainEvent: DomainEvent): Mono<Domain> {
                domainEvent as UserDomainEvent
                return userStateManager.rebuildUser(domainEvent.userId, domainEvent.operationId).map { it }
            }

            override fun mapDomainEvent() = mapper().toMono()

            override fun saveEvent(domainEvent: DomainEvent): Mono<DomainEvent> = userStateManager.saveEvent(domainEvent).map { it }

            override fun handleError(error: Throwable) = userStateManager.handleError(sagaEvent, error)

            override fun createResponseSagaEvent(domain: Domain) =
                SagaEvent(sagaEvent.type.approvedEventType!!, sagaEvent.operationId, ServiceEnum.USER_SERVICE, sagaEvent.responsibleUserId, domain.toDto()).toMono()

        }.handleEvent()
    }
}
