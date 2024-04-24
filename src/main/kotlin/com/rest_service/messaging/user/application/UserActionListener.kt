package com.rest_service.messaging.user.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class UserActionListener(
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
