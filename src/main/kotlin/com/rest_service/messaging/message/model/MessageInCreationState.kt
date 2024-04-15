package com.rest_service.messaging.message.model

import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType

class MessageInCreationState(private val domain: MessageDomain) : MessageState {
    override fun apply(event: MessageDomainEvent): MessageDomainEvent {
        when (event.type) {
            MessageDomainEventType.MESSAGE_CREATED -> MessageCreatedState(domain, event)
            else                                   ->
                throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        }.let { domain.changeState(it) }

        return event
    }

    override fun createResponseEvent() = throw UnsupportedOperationException("No next event for room in creation state.")
}
