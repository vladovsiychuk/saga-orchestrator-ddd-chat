package com.rest_service.messaging.message.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.MessageUpdateCommand
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent

class MessageUpdatedState(private val domain: MessageDomain) : MessageState {
    constructor(domain: MessageDomain, event: MessageDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, MessageUpdateCommand::class.java)
        domain.message.content = command.content
    }

    private val mapper = jacksonObjectMapper()

    override fun apply(event: MessageDomainEvent) = run {
        MessageCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
