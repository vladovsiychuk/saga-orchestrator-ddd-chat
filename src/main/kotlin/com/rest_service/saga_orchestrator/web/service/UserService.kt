package com.rest_service.saga_orchestrator.web.service

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.UserCreateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.web.ResponseDTO
import com.rest_service.saga_orchestrator.web.request.UserCreateRequest
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
