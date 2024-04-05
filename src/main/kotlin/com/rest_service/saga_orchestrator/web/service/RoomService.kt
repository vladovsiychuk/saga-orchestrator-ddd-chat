package com.rest_service.saga_orchestrator.web.service

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.web.ResponseDTO
import com.rest_service.saga_orchestrator.web.request.RoomAddMemberRequest
import com.rest_service.saga_orchestrator.web.request.RoomCreateRequest
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
class RoomService(
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val securityManager: SecurityManager,
) {
    fun startCreateRoom(request: RoomCreateRequest): Mono<ResponseDTO> {
        return securityManager.getCurrentUserIdAndEmail()
            .map { (currentUserId, currentUserEmail) ->
                val command = RoomCreateCommand(
                    request.companionUserId
                )

                SagaEvent(
                    SagaEventType.ROOM_CREATE_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    currentUserEmail,
                    currentUserId,
                    command
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }

    fun startAddMember(roomId: UUID, command: RoomAddMemberRequest): Mono<ResponseDTO> {
        return securityManager.getCurrentUserIdAndEmail()
            .map { (currentUserId, currentUserEmail) ->
                val addMemberCommand = RoomAddMemberCommand(roomId, command.memberId)

                SagaEvent(
                    SagaEventType.ROOM_ADD_MEMBER_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    currentUserEmail,
                    currentUserId,
                    addMemberCommand
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }
}
