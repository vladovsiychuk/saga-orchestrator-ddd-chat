package com.rest_service.saga_orchestrator.application

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.enums.SagaType
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.model.UserCreateSagaState
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
open class UserCreateSagaEventHandler(
    repository: SagaEventRepository,
    applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
    securityManager: SecurityManager,
    private val eventFactory: EventFactory,
) : AbstractSagaEventHandler(repository, applicationEventPublisher, securityManager) {
    override fun shouldHandle(sagaType: SagaType): Boolean {
        return sagaType in listOf(SagaType.USER_CREATE_START, SagaType.USER_CREATE_APPROVE, SagaType.USER_CREATE_REJECT)
    }

    override fun createNewState(operationId: UUID) = UserCreateSagaState(operationId, eventFactory)

    override fun getRejectSagaType() = SagaType.USER_CREATE_REJECT
}
