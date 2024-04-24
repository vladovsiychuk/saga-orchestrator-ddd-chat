package com.rest_service.messaging.message.model

import com.rest_service.messaging.message.infrastructure.MessageDomainEvent

class MessageReadState(private val domain: MessageDomain) : MessageState {
    constructor(domain: MessageDomain, event: MessageDomainEvent) : this(domain) {
        domain.message.read.add(event.responsibleUserId)
    }

    override fun apply(event: MessageDomainEvent) = run {
        MessageCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
