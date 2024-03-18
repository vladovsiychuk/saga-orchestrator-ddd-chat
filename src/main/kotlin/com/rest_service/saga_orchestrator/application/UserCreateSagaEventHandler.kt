package com.rest_service.saga_orchestrator.application

import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.model.UserCreateSagaState
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
open class UserCreateSagaEventHandler(
    private val repository: SagaEventRepository,
    applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
    securityManager: SecurityManager,
    private val eventFactory: EventFactory,
) : AbstractEventHandler(applicationEventPublisher, securityManager) {
    override fun shouldHandle(sagaType: SagaType): Boolean {
        return sagaType in listOf(SagaType.USER_CREATE_START, SagaType.USER_CREATE_APPROVE, SagaType.USER_CREATE_REJECT)
    }

    override fun createNewState(operationId: UUID) = UserCreateSagaState(operationId, eventFactory)
    override fun getRejectSagaType() = SagaType.USER_CREATE_REJECT
    override fun saveEvent(newEvent: SagaEvent) = repository.save(newEvent)
    override fun findSagaEventsByOperationId(operationId: UUID) =
        repository.findByOperationIdOrderByDateCreated(operationId)

    override fun findRejectedEvent(operationId: UUID) =
        repository.findByOperationIdAndType(operationId, getRejectSagaType())

    override fun getServiceName() = ServiceEnum.SAGA_SERVICE
}
