package com.rest_service.saga_orchestrator.web.service

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.UserCreateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.commons.enums.UserType
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.web.ResponseDTO
import com.rest_service.saga_orchestrator.web.request.UserCreateRequest
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
class UserService(
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val securityManager: SecurityManager,
) {
    fun startCreateCurrentUser(request: UserCreateRequest): Mono<ResponseDTO> {
        val currentUserEmail = securityManager.getCurrentUserEmail()
        val operationId = UUID.randomUUID()

        val translationLanguages = if (request.type == UserType.TRANSLATOR) {
            (request.translationLanguages ?: mutableSetOf()).apply {
                add(request.primaryLanguage)
            }
        } else null

        val command = UserCreateCommand(
            request.type,
            request.username,
            currentUserEmail,
            request.primaryLanguage,
            translationLanguages,
            request.temporaryId
        )

        val sagaEvent = SagaEvent(
            SagaEventType.USER_CREATE_START,
            operationId,
            ServiceEnum.SAGA_SERVICE,
            currentUserEmail,
            null,
            command
        )

        return command.validate()
            .doOnNext { applicationEventPublisher.publishEventAsync(sagaEvent) }
            .map { ResponseDTO(operationId) }
    }
}
