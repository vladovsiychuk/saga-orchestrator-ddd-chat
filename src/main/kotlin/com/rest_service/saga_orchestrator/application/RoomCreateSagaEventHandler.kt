package com.rest_service.saga_orchestrator.application

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.enums.SagaType
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import com.rest_service.saga_orchestrator.model.RoomCreateSagaState
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID

@Singleton
open class RoomCreateSagaEventHandler(
    repository: SagaEventRepository,
    applicationEventPublisher: ApplicationEventPublisher<DomainEvent>,
    securityManager: SecurityManager,
    private val eventFactory: EventFactory,
) : AbstractSagaEventHandler(repository, applicationEventPublisher, securityManager) {
    override fun shouldHandle(sagaType: SagaType): Boolean {
        return sagaType in listOf(SagaType.ROOM_CREATE_START, SagaType.ROOM_CREATE_APPROVE, SagaType.ROOM_CREATE_REJECT)
    }

    override fun createNewState(operationId: UUID) = RoomCreateSagaState(operationId, eventFactory)
    override fun getRejectSagaType() = SagaType.ROOM_CREATE_REJECT

}
