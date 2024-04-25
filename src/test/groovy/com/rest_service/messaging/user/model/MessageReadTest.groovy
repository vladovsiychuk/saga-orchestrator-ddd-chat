package com.rest_service.messaging.user.model

import spock.lang.Specification

import static UserDSL.aUser
import static UserDSL.the
import static com.rest_service.Fixture.anyValidMessageReadCommand
import static com.rest_service.Fixture.anyValidUserCreateCommand
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.MESSAGE_READ_APPROVED
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.rest_service.messaging.user.model.UserDomainEventDSL.anEvent

class MessageReadTest extends Specification {

    def 'should approve message read event when the responsible user just exists'() {
        given: 'an existing regular user'
        def user = aUser()
        def createdEvent = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()
        the user reactsTo createdEvent

        and: 'request to approve the message read'
        def messageReadEvent = anEvent() ofType MESSAGE_READ_APPROVED withPayload anyValidMessageReadCommand()

        when:
        the user reactsTo messageReadEvent

        then:
        (the user data()) != null
    }

    def 'should throw an error when trying to approve the message read for not yet created user'() {
        given: 'a user in InCreation state'
        def user = aUser()

        and: 'request to approve the room creation'
        def roomCreatedEvent = anEvent() ofType MESSAGE_READ_APPROVED withPayload anyValidMessageReadCommand()

        when:
        the user reactsTo roomCreatedEvent

        then:
        thrown(RuntimeException)
    }
}
