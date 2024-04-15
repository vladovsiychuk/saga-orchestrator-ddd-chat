package com.rest_service.messaging.message.model

import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import java.util.UUID
import reactor.core.publisher.Mono

class MessageDomain(
    var operationId: UUID,
    var responsibleUserEmail: String,
    val responsibleUserId: UUID,
    var validateCommands: Boolean = true,
) : Domain {
    private var state: MessageState = MessageInCreationState(this)
    var message: MessageDTO? = null

    override fun apply(event: DomainEvent): DomainEvent {
        return state.apply(event as MessageDomainEvent)
    }

    override fun createResponseSagaEvent(): Mono<SagaEvent> {
        return state.createResponseEvent()
    }

    fun changeState(newState: MessageState) {
        state = newState
    }
}
