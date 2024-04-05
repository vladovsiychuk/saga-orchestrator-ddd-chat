package com.rest_service.messaging.message.model

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import reactor.kotlin.core.publisher.toMono

class MessageCreatedState(private val domain: MessageDomain) : MessageState {
    override fun apply(event: MessageDomainEvent): MessageDomainEvent {
        return when (event.type) {
            else -> throw UnsupportedOperationException()
        }
    }

    override fun createResponseEvent() = SagaEvent(SagaEventType.MESSAGE_CREATE_APPROVED, domain.operationId, ServiceEnum.MESSAGE_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId, domain.message!!).toMono()
}
