package com.rest_service.saga_orchestrator.infrastructure

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.Command
import com.rest_service.commons.command.RoomCommand
import com.rest_service.commons.command.UserCommand
import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class EventFactory(private val securityManager: SecurityManager) {
    fun createStartEvent(command: Command): Mono<DomainEvent> {
        val type: EventType = when (command) {
            is RoomCommand -> EventType.ROOM_CREATE_START
            is UserCommand -> EventType.USER_CREATE_START
            else           -> throw UnsupportedOperationException()
        }

        return DomainEvent(
            type,
            UUID.randomUUID(),
            ServiceEnum.SAGA_SERVICE,
            securityManager.getUserEmail(),
            command
        ).toMono()
    }

    fun createEvent(type: EventType, operationId: UUID, responsibleUserEmail: String, payload: Any): Mono<DomainEvent> =
        DomainEvent(
            type,
            operationId,
            ServiceEnum.SAGA_SERVICE,
            responsibleUserEmail,
            payload
        ).toMono()
}
