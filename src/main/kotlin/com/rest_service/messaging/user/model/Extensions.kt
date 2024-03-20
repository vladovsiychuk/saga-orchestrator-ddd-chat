package com.rest_service.messaging.user.model

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.UserCommand
import com.rest_service.messaging.user.infrastructure.UserDomainEvent

fun DomainEvent.convertEvent(): UserDomainEvent {
    val mapper = jacksonObjectMapper()
    val command = mapper.convertValue(this.payload, UserCommand::class.java)

    return UserDomainEvent(
        userId = command.id!!,
        email = command.email,
        payload = mapper.convertValue(this.payload),
        type = this.type,
        operationId = this.operationId
    )
}
