package com.rest_service.messaging.user.application

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class UserActionListener(
    private val userCreateInitiatedEventHandler: UserCreateInitiatedEventHandler,
    private val roomCreateInitiatedEventHandler: RoomCreateInitiatedEventHandler,
    private val roomAddMemberInitiatedEventHandler: RoomAddMemberInitiatedEventHandler,
    private val messageUpdateInitiatedEventHandler: MessageUpdateInitiatedEventHandler,
    private val messageReadInitiatedEventHandler: MessageReadInitiatedEventHandler,
    private val messageTranslateInitiatedEventHandler: MessageTranslateInitiatedEventHandler,
    private val rejectEventHandler: RejectEventHandler,
) {
    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_INITIATED       -> userCreateInitiatedEventHandler.handleEvent(event)
            SagaEventType.ROOM_CREATE_INITIATED       -> roomCreateInitiatedEventHandler.handleEvent(event)
            SagaEventType.ROOM_ADD_MEMBER_INITIATED   -> roomAddMemberInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_UPDATE_INITIATED    -> messageUpdateInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_READ_INITIATED      -> messageReadInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_TRANSLATE_INITIATED -> messageTranslateInitiatedEventHandler.handleEvent(event)
            SagaEventType.USER_CREATE_REJECTED,
            SagaEventType.ROOM_CREATE_REJECTED,
            SagaEventType.ROOM_ADD_MEMBER_REJECTED,
            SagaEventType.MESSAGE_UPDATE_REJECTED,
            SagaEventType.MESSAGE_READ_REJECTED,
            SagaEventType.MESSAGE_TRANSLATE_REJECTED  -> rejectEventHandler.handleEvent(event)

            else                                      -> {}
        }
    }
}
