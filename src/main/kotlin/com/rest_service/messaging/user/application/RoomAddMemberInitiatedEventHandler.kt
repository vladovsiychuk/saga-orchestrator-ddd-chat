package com.rest_service.messaging.user.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
@Named("RoomAddMemberInitiatedEventHandler_userDomain")
class RoomAddMemberInitiatedEventHandler(
    applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val userStateManager: UserStateManager,
) : AbstractEventHandler(applicationEventPublisher) {
    private val mapper = jacksonObjectMapper()
    override fun checkOperationFailed(operationId: UUID) = userStateManager.checkOperationFailed(operationId)

    override fun rebuildDomainFromEvent(event: DomainEvent): Mono<Domain> {
        event as UserDomainEvent
        return userStateManager.rebuildUser(event.userId, event.operationId)
    }

    override fun mapDomainEvent(event: SagaEvent): DomainEvent {
        val command = mapper.convertValue(event.payload, RoomAddMemberCommand::class.java)
        return userStateManager.mapDomainEvent(command.memberId, UserDomainEventType.ROOM_ADD_MEMBER_APPROVED, event)
    }

    override fun saveEvent(event: DomainEvent): Mono<DomainEvent> {
        return userStateManager.saveEvent(event).map { it }
    }

    override fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return userStateManager.handleError(event, error, SagaEventType.ROOM_ADD_MEMBER_REJECTED)
    }
}
