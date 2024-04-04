package com.rest_service.saga_orchestrator.application

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class SagaEventListener(
    private val userCreateSagaEventHandler: UserCreateSagaEventHandler,
    private val roomCreateSagaEventHandler: RoomCreateSagaEventHandler,
    private val roomAddMemberSagaEventHandler: RoomAddMemberSagaEventHandler,
    private val rejectEventHandler: RejectEventHandler,
) {
    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_START,
            SagaEventType.USER_CREATE_APPROVED     -> userCreateSagaEventHandler.handleEvent(event)

            SagaEventType.ROOM_CREATE_START,
            SagaEventType.ROOM_CREATE_APPROVED     -> roomCreateSagaEventHandler.handleEvent(event)

            SagaEventType.ROOM_ADD_MEMBER_START,
            SagaEventType.ROOM_ADD_MEMBER_APPROVED -> roomAddMemberSagaEventHandler.handleEvent(event)

            SagaEventType.USER_CREATE_REJECTED,
            SagaEventType.ROOM_CREATE_REJECTED,
            SagaEventType.ROOM_ADD_MEMBER_REJECTED -> rejectEventHandler.handleEvent(event)

            else                                   -> {}
        }
    }
}
