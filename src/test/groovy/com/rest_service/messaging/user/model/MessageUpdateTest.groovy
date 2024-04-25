package com.rest_service.messaging.user.model


import spock.lang.Specification

import static UserDSL.aUser
import static UserDSL.the
import static com.rest_service.Fixture.anyValidMessageUpdateCommand
import static com.rest_service.Fixture.anyValidUserCreateCommand
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.MESSAGE_UPDATE_APPROVED
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.rest_service.messaging.user.model.UserDomainEventDSL.anEvent

class MessageUpdateTest extends Specification {

    def 'should approve message update event when the responsible user exists'() {
        given: 'an existing regular user'
        def user = aUser()
        def createdEvent = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()
        the user reactsTo createdEvent

        and: 'request to approve the message update'
        def messageUpdatedEvent = anEvent() ofType MESSAGE_UPDATE_APPROVED withPayload anyValidMessageUpdateCommand()

        when:
        the user reactsTo messageUpdatedEvent

        then:
        (the user data()) != null
    }

    def 'should throw an error when trying to approve the message update for not yet created user'() {
        given: 'a user in InCreation state'
        def user = aUser()

        and: 'request to approve the room creation'
        def messageUpdatedEvent = anEvent() ofType MESSAGE_UPDATE_APPROVED withPayload anyValidMessageUpdateCommand()

        when:
        the user reactsTo messageUpdatedEvent

        then:
        thrown(RuntimeException)
    }
}
