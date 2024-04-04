package com.rest_service.messaging.room.application

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class RoomActionListener(
    private val roomCreateInitiatedEventHandler: RoomCreateInitiatedEventHandler,
    private val roomAddMemberInitiatedEventHandler: RoomAddMemberInitiatedEventHandler,
) {
    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.ROOM_CREATE_INITIATED     -> roomCreateInitiatedEventHandler.handleEvent(event)
            SagaEventType.ROOM_ADD_MEMBER_INITIATED -> roomAddMemberInitiatedEventHandler.handleEvent(event)
            else                                    -> {}
        }
    }
}
