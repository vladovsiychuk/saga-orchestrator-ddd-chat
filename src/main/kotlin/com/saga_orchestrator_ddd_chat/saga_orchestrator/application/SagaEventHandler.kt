package com.saga_orchestrator_ddd_chat.saga_orchestrator.application

import com.saga_orchestrator_ddd_chat.commons.AbstractEventHandler
import com.saga_orchestrator_ddd_chat.commons.Domain
import com.saga_orchestrator_ddd_chat.commons.DomainEvent
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaDomainEvent
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.AbstractSagaStateManager
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.MessageCreateSaga
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.MessageReadSaga
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.MessageTranslateSaga
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.MessageUpdateSaga
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.RoomAddMemberSaga
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.RoomCreateSaga
import com.saga_orchestrator_ddd_chat.saga_orchestrator.model.UserCreateSaga
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class SagaEventHandler(
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val sagaStateManager: SagaStateManager,
) {
    @EventListener
    @Async
    open fun sagaActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_START,
            SagaEventType.USER_CREATE_APPROVED,
            SagaEventType.USER_CREATE_REJECTED       -> handleEvent(event) {
                UserCreateSaga(it.operationId, it.responsibleUserId)
            }

            SagaEventType.ROOM_CREATE_START,
            SagaEventType.ROOM_CREATE_APPROVED,
            SagaEventType.ROOM_CREATE_REJECTED       -> handleEvent(event) {
                RoomCreateSaga(it.operationId, it.responsibleUserId)
            }

            SagaEventType.ROOM_ADD_MEMBER_START,
            SagaEventType.ROOM_ADD_MEMBER_APPROVED,
            SagaEventType.ROOM_ADD_MEMBER_REJECTED   -> handleEvent(event) {
                RoomAddMemberSaga(it.operationId, it.responsibleUserId)
            }

            SagaEventType.MESSAGE_CREATE_START,
            SagaEventType.MESSAGE_CREATE_APPROVED,
            SagaEventType.MESSAGE_CREATE_REJECTED    -> handleEvent(event) {
                MessageCreateSaga(it.operationId, it.responsibleUserId)
            }

            SagaEventType.MESSAGE_UPDATE_START,
            SagaEventType.MESSAGE_UPDATE_APPROVED,
            SagaEventType.MESSAGE_UPDATE_REJECTED    -> handleEvent(event) {
                MessageUpdateSaga(it.operationId, it.responsibleUserId)
            }

            SagaEventType.MESSAGE_READ_START,
            SagaEventType.MESSAGE_READ_APPROVED,
            SagaEventType.MESSAGE_READ_REJECTED      -> handleEvent(event) {
                MessageReadSaga(it.operationId, it.responsibleUserId)
            }

            SagaEventType.MESSAGE_TRANSLATE_START,
            SagaEventType.MESSAGE_TRANSLATE_APPROVED,
            SagaEventType.MESSAGE_TRANSLATE_REJECTED -> handleEvent(event) {
                MessageTranslateSaga(it.operationId, it.responsibleUserId)
            }

            else                                     -> {}
        }
    }

    private fun handleEvent(
        sagaEvent: SagaEvent,
        createSaga: (domainEvent: SagaDomainEvent) -> AbstractSagaStateManager<*, *>
    ) {
        object : AbstractEventHandler(applicationEventPublisher) {
            override fun rebuildDomainFromEvent(domainEvent: DomainEvent): Mono<Domain> {
                domainEvent as SagaDomainEvent
                return createSaga(domainEvent)
                    .let { specificSaga ->
                        sagaStateManager.rebuildSaga(domainEvent, specificSaga)
                    }
            }

            override fun mapDomainEvent() = sagaStateManager.mapDomainEvent(sagaEvent).toMono()

            override fun saveEvent(domainEvent: DomainEvent): Mono<DomainEvent> = sagaStateManager.saveEvent(domainEvent).map { it }

            override fun handleError(error: Throwable) = sagaStateManager.handleError(sagaEvent, error)

            override fun createResponseSagaEvent(domain: Domain) =
                (domain as AbstractSagaStateManager<*, *>).createResponseSagaEvent()

        }.handleEvent()
    }
}
