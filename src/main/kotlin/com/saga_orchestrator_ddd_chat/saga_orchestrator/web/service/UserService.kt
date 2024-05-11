package com.saga_orchestrator_ddd_chat.saga_orchestrator.web.service

import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.command.UserCreateCommand
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SecurityManager
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.ResponseDTO
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.UserCreateRequest
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class UserService(
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val securityManager: SecurityManager,
) {
    fun startCreateCurrentUser(request: UserCreateRequest): Mono<ResponseDTO> {
        return securityManager.getCurrentUserEmail().toMono()
            .map { currentUserEmail ->
                val command = UserCreateCommand(
                    request.type,
                    request.username,
                    currentUserEmail,
                    request.primaryLanguage,
                    request.translationLanguages,
                )

                SagaEvent(
                    SagaEventType.USER_CREATE_START,
                    UUID.randomUUID(),
                    ServiceEnum.SAGA_SERVICE,
                    UUID.nameUUIDFromBytes(currentUserEmail.toByteArray()),
                    command
                )
            }
            .doOnNext { applicationEventPublisher.publishEventAsync(it) }
            .map { ResponseDTO(it.operationId) }
    }
}
