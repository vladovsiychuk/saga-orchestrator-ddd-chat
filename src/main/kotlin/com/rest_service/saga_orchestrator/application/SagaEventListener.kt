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
    private val messageCreateSagaEventHandler: MessageCreateSagaEventHandler,
    private val messageUpdateSagaEventHandler: MessageUpdateSagaEventHandler,
    private val messageReadSagaEventHandler: MessageReadSagaEventHandler,
    private val messageTranslateSagaEventHandler: MessageTranslateSagaEventHandler,
) {
    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_START,
            SagaEventType.USER_CREATE_APPROVED,
            SagaEventType.USER_CREATE_REJECTED       -> userCreateSagaEventHandler.handleEvent(event)

            SagaEventType.ROOM_CREATE_START,
            SagaEventType.ROOM_CREATE_APPROVED,
            SagaEventType.ROOM_CREATE_REJECTED       -> roomCreateSagaEventHandler.handleEvent(event)

            SagaEventType.ROOM_ADD_MEMBER_START,
            SagaEventType.ROOM_ADD_MEMBER_APPROVED,
            SagaEventType.ROOM_ADD_MEMBER_REJECTED   -> roomAddMemberSagaEventHandler.handleEvent(event)

            SagaEventType.MESSAGE_CREATE_START,
            SagaEventType.MESSAGE_CREATE_APPROVED,
            SagaEventType.MESSAGE_CREATE_REJECTED    -> messageCreateSagaEventHandler.handleEvent(event)

            SagaEventType.MESSAGE_UPDATE_START,
            SagaEventType.MESSAGE_UPDATE_APPROVED,
            SagaEventType.MESSAGE_UPDATE_REJECTED    -> messageUpdateSagaEventHandler.handleEvent(event)

            SagaEventType.MESSAGE_READ_START,
            SagaEventType.MESSAGE_READ_APPROVED,
            SagaEventType.MESSAGE_READ_REJECTED      -> messageReadSagaEventHandler.handleEvent(event)

            SagaEventType.MESSAGE_TRANSLATE_START,
            SagaEventType.MESSAGE_TRANSLATE_APPROVED,
            SagaEventType.MESSAGE_TRANSLATE_REJECTED -> messageTranslateSagaEventHandler.handleEvent(event)

            else                                     -> {}
        }
    }
}
