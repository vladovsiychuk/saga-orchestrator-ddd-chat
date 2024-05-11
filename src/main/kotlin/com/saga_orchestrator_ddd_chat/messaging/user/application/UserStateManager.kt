package com.saga_orchestrator_ddd_chat.messaging.user.application

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.dto.ErrorDTO
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEvent
import com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventRepository
import com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventType
import com.saga_orchestrator_ddd_chat.messaging.user.model.User
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class UserStateManager(
    private val repository: UserDomainEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
) {
    private val mapper = jacksonObjectMapper()

    fun rebuildUser(userId: UUID, operationId: UUID): Mono<User> {
        return repository.findDomainEvents(userId)
            .takeUntil { it.operationId == operationId }
            .reduce(User()) { domain, event ->
                domain.apply(event)
                domain
            }
    }

    fun mapDomainEvent(userId: UUID, type: UserDomainEventType, event: SagaEvent): DomainEvent {
        return UserDomainEvent(
            userId = userId,
            payload = mapper.convertValue(event.payload),
            type = type,
            operationId = event.operationId,
            responsibleUserId = event.responsibleUserId,
        )
    }

    fun saveEvent(event: DomainEvent): Mono<UserDomainEvent> {
        return repository.save(event as UserDomainEvent)
    }

    fun handleError(event: SagaEvent, error: Throwable): Mono<Void> {
        return checkOperationFailed(event.operationId)
            .flatMap {
                val errorEvent = SagaEvent(
                    event.type.rejectedEventType!!,
                    event.operationId,
                    ServiceEnum.USER_SERVICE,
                    event.responsibleUserId,
                    ErrorDTO(error.message),
                )
                applicationEventPublisher.publishEventAsync(errorEvent)
                Mono.error(error)
            }
    }

    private fun checkOperationFailed(operationId: UUID): Mono<Boolean> {
        return repository.existsByOperationIdAndType(operationId, UserDomainEventType.UNDO)
            .flatMap {
                if (it)
                    return@flatMap Mono.empty()
                else
                    true.toMono()
            }
    }
}
