package com.rest_service.messaging.message.model

import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidMessageCreateCommand
import static com.rest_service.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_CREATED
import static com.rest_service.messaging.message.model.MessageDomainDSL.aMessage
import static com.rest_service.messaging.message.model.MessageDomainDSL.the
import static com.rest_service.messaging.message.model.MessageDomainEventDSL.anEvent

class MessageCreateTest extends Specification {

    def 'should approve message creation on successful initiate event'() {
        given:
        def room = aMessage()

        and: 'a user create event'
        def roomCreatedEvent = anEvent() ofType MESSAGE_CREATED withPayload anyValidMessageCreateCommand()

        when:
        the room reactsTo roomCreatedEvent

        then:
        (the room responseEvent() type) == SagaEventType.MESSAGE_CREATE_APPROVED
    }
}
