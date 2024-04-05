package com.rest_service.messaging.message.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.MessageCreateCommand
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType

class MessageInCreationState(private val domain: MessageDomain) : MessageState {
    private val mapper = jacksonObjectMapper()

    override fun apply(event: MessageDomainEvent): MessageDomainEvent {
        return when (event.type) {
            MessageDomainEventType.MESSAGE_CREATED -> createMessage(event)
            else                                   -> throw UnsupportedOperationException()
        }
    }

    private fun createMessage(event: MessageDomainEvent): MessageDomainEvent {
        val command = mapper.convertValue(event.payload, MessageCreateCommand::class.java)

        domain.message = MessageDTO(command, event)
        domain.changeState(MessageCreatedState(domain))
        return event
    }

    override fun createResponseEvent() = throw UnsupportedOperationException("No next event for room in creation state.")
}
