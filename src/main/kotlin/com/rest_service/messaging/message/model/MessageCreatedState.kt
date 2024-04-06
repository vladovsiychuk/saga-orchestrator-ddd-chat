package com.rest_service.messaging.message.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.MessageUpdateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType
import reactor.kotlin.core.publisher.toMono

class MessageCreatedState(private val domain: MessageDomain) : MessageState {
    private val mapper = jacksonObjectMapper()

    override fun apply(event: MessageDomainEvent): MessageDomainEvent {
        return when (event.type) {
            MessageDomainEventType.MESSAGE_UPDATED -> updateMessage(event)
            else                                   -> throw UnsupportedOperationException()
        }
    }

    private fun updateMessage(event: MessageDomainEvent): MessageDomainEvent {
        val command = mapper.convertValue(event.payload, MessageUpdateCommand::class.java)
        domain.message!!.content = command.content
        domain.changeState(MessageUpdatedState(domain))
        return event
    }

    override fun createResponseEvent() = SagaEvent(SagaEventType.MESSAGE_CREATE_APPROVED, domain.operationId, ServiceEnum.MESSAGE_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId, domain.message!!).toMono()
}
