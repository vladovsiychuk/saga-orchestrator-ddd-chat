package com.rest_service.messaging.message.application

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class MessageActionListener(
    private val messageCreateInitiatedEventHandler: MessageCreateInitiatedEventHandler,
    private val messageUpdateInitiatedEventHandler: MessageUpdateInitiatedEventHandler,
) {
    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.MESSAGE_CREATE_INITIATED -> messageCreateInitiatedEventHandler.handleEvent(event)
            SagaEventType.MESSAGE_UPDATE_INITIATED -> messageUpdateInitiatedEventHandler.handleEvent(event)
            else                                   -> {}
        }
    }
}
