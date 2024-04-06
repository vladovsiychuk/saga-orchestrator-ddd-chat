package com.rest_service.messaging.message.model

import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidMessageCreateCommand
import static com.rest_service.Fixture.anyValidMessageUpdateCommand
import static com.rest_service.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_CREATED
import static com.rest_service.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_UPDATED
import static com.rest_service.messaging.message.model.MessageDomainDSL.aMessage
import static com.rest_service.messaging.message.model.MessageDomainDSL.the
import static com.rest_service.messaging.message.model.MessageDomainEventDSL.anEvent

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
        (the message responseEvent() type) == SagaEventType.MESSAGE_UPDATE_APPROVED

        and:
        message.domain.message.content == 'new content'
    }
}
