package com.rest_service.messaging.message.model

import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidMessageCreateCommand
import static com.rest_service.Fixture.anyValidMessageReadCommand
import static com.rest_service.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_CREATED
import static com.rest_service.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_READ
import static com.rest_service.messaging.message.model.MessageDomainDSL.aMessage
import static com.rest_service.messaging.message.model.MessageDomainDSL.the
import static com.rest_service.messaging.message.model.MessageDomainEventDSL.anEvent

class MessageReadTest extends Specification {

    def 'should approve message read on successful initiate event'() {
        given: 'a created message from a specific user'
        def message = aMessage()
        def messageCreatedEvent = anEvent() ofType MESSAGE_CREATED withPayload anyValidMessageCreateCommand()
        the message reactsTo messageCreatedEvent

        and: 'a message read event'
        def currentUserId = UUID.randomUUID()
        def messageReadEvent = anEvent() from currentUserId ofType MESSAGE_READ withPayload anyValidMessageReadCommand()

        when:
        the message reactsTo messageReadEvent

        then:
        (the message responseEvent() type) == SagaEventType.MESSAGE_READ_APPROVED

        and:
        message.domain.message.read.contains(currentUserId)
    }
}
