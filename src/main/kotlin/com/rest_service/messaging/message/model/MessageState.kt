package com.rest_service.messaging.message.model

import com.rest_service.messaging.message.infrastructure.MessageDomainEvent

interface MessageState {
    fun apply(event: MessageDomainEvent): MessageDomainEvent
}
