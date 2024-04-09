package com.rest_service.messaging.room.application

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class RoomActionListener(
    private val rejectEventHandler: RejectEventHandler,
    private val roomCreateInitiatedEventHandler: RoomCreateInitiatedEventHandler,
    private val roomAddMemberInitiatedEventHandler: RoomAddMemberInitiatedEventHandler,
    private val messageCreateInitiatedEventHandler: MessageCreateInitiatedEventHandler,
    private val messageReadInitiatedEventHandler: MessageReadInitiatedEventHandler,
    private val messageTranslateInitiatedEventHandler: MessageTranslateInitiatedEventHandler,
) {
    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.ROOM_CREATE_INITIATED       -> roomCreateInitiatedEventHandler.handleEvent(event)
            SagaEventType.ROOM_ADD_MEMBER_INITIATED   -> roomAddMemberInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_CREATE_INITIATED    -> messageCreateInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_READ_INITIATED      -> messageReadInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_TRANSLATE_INITIATED -> messageTranslateInitiatedEventHandler.handleEvent(event)
            SagaEventType.ROOM_CREATE_REJECTED,
            SagaEventType.ROOM_ADD_MEMBER_REJECTED,
            SagaEventType.MESSAGE_CREATE_REJECTED,
            SagaEventType.MESSAGE_READ_REJECTED,
            SagaEventType.MESSAGE_TRANSLATE_REJECTED  -> rejectEventHandler.handleEvent(event)

            else                                      -> {}
        }
    }
}
