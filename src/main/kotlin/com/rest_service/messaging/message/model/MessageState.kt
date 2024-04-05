package com.rest_service.messaging.message.model

import com.rest_service.commons.SagaEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import reactor.core.publisher.Mono

interface MessageState {
    fun apply(event: MessageDomainEvent): MessageDomainEvent
    fun createResponseEvent(): Mono<SagaEvent>
}
