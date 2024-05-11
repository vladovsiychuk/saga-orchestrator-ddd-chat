package com.saga_orchestrator_ddd_chat.saga_orchestrator.web.service

import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.command.MessageCreateCommand
import com.saga_orchestrator_ddd_chat.commons.command.MessageReadCommand
import com.saga_orchestrator_ddd_chat.commons.command.MessageTranslateCommand
import com.saga_orchestrator_ddd_chat.commons.command.MessageUpdateCommand
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SecurityManager
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.ResponseDTO
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.MessageCreateRequest
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.MessageTranslateRequest
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.MessageUpdateRequest
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class MessageService(
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val securityManager: SecurityManager,
) {
    fun startCreateMessage(request: MessageCreateRequest): Mono<ResponseDTO> {
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
                    currentUser.id,
                    command
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }

    fun update(command: MessageUpdateRequest, messageId: UUID): Mono<ResponseDTO> {
        return securityManager.getCurrentUserEmail().toMono()
            .map { currentUserEmail ->
                SagaEvent(
                    SagaEventType.MESSAGE_UPDATE_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    UUID.nameUUIDFromBytes(currentUserEmail.toByteArray()),
                    MessageUpdateCommand(messageId, command.content)
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }

    fun read(messageId: UUID): Mono<ResponseDTO> {
        return securityManager.getCurrentUserEmail().toMono()
            .map { currentUserEmail ->
                SagaEvent(
                    SagaEventType.MESSAGE_READ_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    UUID.nameUUIDFromBytes(currentUserEmail.toByteArray()),
                    MessageReadCommand(messageId)
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }

    fun translate(command: MessageTranslateRequest, messageId: UUID): Mono<ResponseDTO> {
        return securityManager.getCurrentUserEmail().toMono()
            .map { currentUserEmail ->
                SagaEvent(
                    SagaEventType.MESSAGE_TRANSLATE_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    UUID.nameUUIDFromBytes(currentUserEmail.toByteArray()),
                    MessageTranslateCommand(messageId, command.translation, command.language)
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }
}
