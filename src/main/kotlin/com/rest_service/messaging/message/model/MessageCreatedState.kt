package com.rest_service.messaging.message.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.MessageCreateCommand
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType

class MessageCreatedState(private val domain: MessageDomain) : MessageState {
    constructor(domain: MessageDomain, event: MessageDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, MessageCreateCommand::class.java)

        domain.message = MessageDTO(command, event)
    }

    private val mapper = jacksonObjectMapper()

    override fun apply(event: MessageDomainEvent): MessageDomainEvent {
        when (event.type) {
            MessageDomainEventType.MESSAGE_UPDATED    -> MessageUpdatedState(domain, event)
            MessageDomainEventType.MESSAGE_READ       -> MessageReadState(domain, event)
            MessageDomainEventType.MESSAGE_TRANSLATED -> MessageTranslateState(domain, event)
            else                                      ->
                throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        }.let { domain.changeState(it) }

        return event
    }
}
