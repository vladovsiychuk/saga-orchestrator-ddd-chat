package com.rest_service.messaging.message.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.MessageUpdateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import reactor.kotlin.core.publisher.toMono

class MessageUpdatedState(private val domain: MessageDomain) : MessageState {
    constructor(domain: MessageDomain, event: MessageDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, MessageUpdateCommand::class.java)
        domain.message!!.content = command.content
    }

    private val mapper = jacksonObjectMapper()

    override fun apply(event: MessageDomainEvent) = run {
        MessageCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }

    override fun createResponseEvent() = SagaEvent(SagaEventType.MESSAGE_UPDATE_APPROVED, domain.operationId, ServiceEnum.MESSAGE_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId, domain.message!!).toMono()
}
