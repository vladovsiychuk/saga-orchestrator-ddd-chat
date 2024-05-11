package com.saga_orchestrator_ddd_chat.messaging.message.model


import spock.lang.Specification

import static MessageDSL.aMessage
import static MessageDSL.the
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidMessageCreateCommand
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidMessageReadCommand
import static com.saga_orchestrator_ddd_chat.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_CREATED
import static com.saga_orchestrator_ddd_chat.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_READ
import static com.saga_orchestrator_ddd_chat.messaging.message.model.MessageDomainEventDSL.anEvent

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
        (the message data()).read.contains(currentUserId)
    }

    def 'should throw an error when trying read non-existing message'() {
        given: 'a message in InCreation state'
        def message = aMessage()

        and: 'a message read event'
        def messageReadEvent = anEvent() ofType MESSAGE_READ withPayload anyValidMessageReadCommand()

        when:
        the message reactsTo messageReadEvent

        then:
        thrown(RuntimeException)
    }
}
