package com.saga_orchestrator_ddd_chat.saga_orchestrator.web.service

import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.command.RoomAddMemberCommand
import com.saga_orchestrator_ddd_chat.commons.command.RoomCreateCommand
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SecurityManager
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.ResponseDTO
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.RoomAddMemberRequest
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.RoomCreateRequest
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class RoomService(
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val securityManager: SecurityManager,
) {
    fun startCreateRoom(request: RoomCreateRequest): Mono<ResponseDTO> {
        return securityManager.getCurrentUserEmail().toMono()
            .map { currentUserEmail ->
                val command = RoomCreateCommand(
                    request.companionUserId
                )

                SagaEvent(
                    SagaEventType.ROOM_CREATE_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    UUID.nameUUIDFromBytes(currentUserEmail.toByteArray()),
                    command
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }

    fun startAddMember(roomId: UUID, command: RoomAddMemberRequest): Mono<ResponseDTO> {
        return securityManager.getCurrentUserEmail().toMono()
            .map { currentUserEmail ->
                val addMemberCommand = RoomAddMemberCommand(roomId, command.memberId)

                SagaEvent(
                    SagaEventType.ROOM_ADD_MEMBER_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    UUID.nameUUIDFromBytes(currentUserEmail.toByteArray()),
                    addMemberCommand
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }
}
