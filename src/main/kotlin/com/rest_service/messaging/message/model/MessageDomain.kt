package com.rest_service.messaging.message.model

import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import java.util.UUID
import reactor.core.publisher.Mono

class MessageDomain(var operationId: UUID) : Domain {
    private var state: MessageState = MessageInCreationState(this)
    lateinit var message: MessageDTO

    override fun apply(event: DomainEvent): DomainEvent {
        return state.apply(event as MessageDomainEvent)
    }

    override fun createResponseSagaEvent(sagaEvent: SagaEvent): Mono<SagaEvent> {
        return state.createResponseEvent(sagaEvent)
    }

    fun changeState(newState: MessageState) {
        state = newState
    }
}
