package com.rest_service.messaging.message.model

import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.dto.DTO
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import java.util.UUID

class MessageDomain(var operationId: UUID) : Domain {
    private var state: MessageState = MessageInCreationState(this)
    lateinit var message: MessageDTO

    fun apply(event: MessageDomainEvent): DomainEvent {
        return state.apply(event)
    }

    fun changeState(newState: MessageState) {
        state = newState
    }

    override fun toDto(): DTO {
        return message
    }
}
