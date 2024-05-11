package com.saga_orchestrator_ddd_chat.messaging.message.model


import spock.lang.Specification

import static MessageDSL.aMessage
import static MessageDSL.the
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidMessageCreateCommand
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidMessageUpdateCommand
import static com.saga_orchestrator_ddd_chat.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_CREATED
import static com.saga_orchestrator_ddd_chat.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_UPDATED
import static com.saga_orchestrator_ddd_chat.messaging.message.model.MessageDomainEventDSL.anEvent

class MessageUpdateTest extends Specification {

    def 'should approve message update on successful initiate event'() {
        given: 'a created message'
        def message = aMessage()
        def messageCreatedEvent = anEvent() ofType MESSAGE_CREATED withPayload anyValidMessageCreateCommand()
        the message reactsTo messageCreatedEvent

        and: 'a command with new content'
        def messageUpdatedCommand = anyValidMessageUpdateCommand()
        messageUpdatedCommand['content'] = 'new content'

        and: 'a message updated event'
        def messageUpdatedEvent = anEvent() ofType MESSAGE_UPDATED withPayload messageUpdatedCommand

        when:
        the message reactsTo messageUpdatedEvent

        then:
        (the message data()).content == 'new content'
    }
}
