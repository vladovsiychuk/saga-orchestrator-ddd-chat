package com.rest_service.messaging.user.model

import com.rest_service.commons.enums.EventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidUserCommand
import static com.rest_service.messaging.user.model.UserDomainDSL.aUser
import static com.rest_service.messaging.user.model.UserDomainDSL.the
import static com.rest_service.messaging.user.model.UserDomainEventDSL.anEvent

class UserCreateTest extends Specification {

    def 'should approve user creation on successful initiate event'() {
        given: 'a regular user in creation'
        def user = aUser()

        and: 'a user create event'
        def inputEvent = anEvent() ofType EventType.USER_CREATE_INITIATE withPayload anyValidUserCommand()

        when:
        the user reactsTo inputEvent

        then:
        (the user nextEvent() type) == EventType.USER_CREATE_APPROVE
    }

    def 'should throw an error when trying to create a duplicate user'() {
        given: 'a user domain that has already processed a user creation event'
        def event = anEvent() ofType EventType.USER_CREATE_INITIATE withPayload anyValidUserCommand()
        def user = aUser() reactsTo event

        when: 'the same user creation event is processed again'
        the user reactsTo event

        then: 'an error is thrown indicating the user is already created'
        def e = thrown(RuntimeException)
        e.message == "User example@test.com is already created."
    }

    def 'should throw an error when trying to applying an event to a user in error state'() {
        given: 'a user domain in an error state due to a reject event'
        def operationId = UUID.randomUUID()
        def rejectEvent = anEvent() ofType EventType.USER_CREATE_REJECT withOperationId operationId
        def user = aUser() withOperationId operationId

        the user reactsTo rejectEvent

        and: 'a new user creation event'
        def createEvent = anEvent() ofType EventType.USER_CREATE_INITIATE withPayload anyValidUserCommand()

        when:
        the user reactsTo createEvent

        then:
        def e = thrown(RuntimeException)
        e.message == "User example@test.com is in error state."
    }
}
