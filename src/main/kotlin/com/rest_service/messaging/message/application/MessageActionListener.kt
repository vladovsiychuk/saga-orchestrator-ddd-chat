package com.rest_service.messaging.message.application

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class MessageActionListener(
    private val rejectEventHandler: RejectEventHandler,
    private val messageCreateInitiatedEventHandler: MessageCreateInitiatedEventHandler,
    private val messageUpdateInitiatedEventHandler: MessageUpdateInitiatedEventHandler,
    private val messageReadInitiatedEventHandler: MessageReadInitiatedEventHandler,
    private val messageTranslateInitiatedEventHandler: MessageTranslateInitiatedEventHandler,
) {
    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.MESSAGE_CREATE_INITIATED    -> messageCreateInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_UPDATE_INITIATED    -> messageUpdateInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_READ_INITIATED      -> messageReadInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_TRANSLATE_INITIATED -> messageTranslateInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_CREATE_REJECTED,
            SagaEventType.MESSAGE_UPDATE_REJECTED,
            SagaEventType.MESSAGE_READ_REJECTED,
            SagaEventType.MESSAGE_TRANSLATE_REJECTED  -> rejectEventHandler.handleEvent(event)

            else                                      -> {}
        }
    }
}
