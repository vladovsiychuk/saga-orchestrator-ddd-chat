package com.rest_service.saga_orchestrator.web.service

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.MessageCreateCommand
import com.rest_service.commons.command.MessageReadCommand
import com.rest_service.commons.command.MessageUpdateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.web.ResponseDTO
import com.rest_service.saga_orchestrator.web.request.MessageCreateRequest
import com.rest_service.saga_orchestrator.web.request.MessageUpdateRequest
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
class MessageService(
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val securityManager: SecurityManager,
) {
    fun startCreateRoom(request: MessageCreateRequest): Mono<ResponseDTO> {
        return securityManager.getCurrentUser()
            .map { currentUser ->
                val command = MessageCreateCommand(
                    request.roomId,
                    request.content,
                    currentUser.primaryLanguage,
                )

                SagaEvent(
                    SagaEventType.MESSAGE_CREATE_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    currentUser.email,
                    currentUser.id,
                    command
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }

    fun update(command: MessageUpdateRequest, messageId: UUID): Mono<ResponseDTO> {
        return securityManager.getCurrentUserIdAndEmail()
            .map { (currentUserId, currentUserEmail) ->
                SagaEvent(
                    SagaEventType.MESSAGE_UPDATE_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    currentUserEmail,
                    currentUserId,
                    MessageUpdateCommand(messageId, command.content)
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }

    fun read(messageId: UUID): Mono<ResponseDTO> {
        return securityManager.getCurrentUserIdAndEmail()
            .map { (currentUserId, currentUserEmail) ->
                SagaEvent(
                    SagaEventType.MESSAGE_READ_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    currentUserEmail,
                    currentUserId,
                    MessageReadCommand(messageId)
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }
}
