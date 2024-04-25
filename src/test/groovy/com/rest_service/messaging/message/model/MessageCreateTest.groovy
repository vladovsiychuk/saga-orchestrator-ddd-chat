package com.rest_service.messaging.message.model


import spock.lang.Specification

import static MessageDSL.aMessage
import static MessageDSL.the
import static com.rest_service.Fixture.anyValidMessageCreateCommand
import static com.rest_service.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_CREATED
import static com.rest_service.messaging.message.model.MessageDomainEventDSL.anEvent

class MessageCreateTest extends Specification {

    def 'should approve message creation on successful initiate event'() {
        given:
        def message = aMessage()

        and: 'a message create event'
        def messageCreatedEvent = anEvent() ofType MESSAGE_CREATED withPayload anyValidMessageCreateCommand()

        when:
        the message reactsTo messageCreatedEvent

        then:
        (the message data()) != null
    }
}
