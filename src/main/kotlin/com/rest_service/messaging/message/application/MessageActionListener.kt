package com.rest_service.messaging.message.application

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class MessageActionListener(
    private val messageCreateInitiatedEventHandler: MessageCreateInitiatedEventHandler,
) {
    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.MESSAGE_CREATE_INITIATED -> messageCreateInitiatedEventHandler.handleEvent(event)
            else                                   -> {}
        }
    }
}
