package com.rest_service.messaging.user.model

import spock.lang.Specification

import static com.rest_service.Fixture.anyValidUserCreateCommand
import static com.rest_service.commons.enums.SagaEventType.USER_CREATE_APPROVED
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.rest_service.messaging.user.model.UserDomainDSL.aUser
import static com.rest_service.messaging.user.model.UserDomainDSL.the
import static com.rest_service.messaging.user.model.UserDomainEventDSL.anEvent

class UserCreateTest extends Specification {

    def 'should approve user creation on successful initiate event'() {
        given: 'a regular user in creation'
        def user = aUser()

        and: 'a user create event'
        def inputEvent = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()

        when:
        the user reactsTo inputEvent

        then:
        (the user responseEvent() type) == USER_CREATE_APPROVED
    }

    def 'should throw an error when trying to create a duplicate user'() {
        given: 'a user domain that has already processed a user creation event'
        def event = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()
        def user = aUser() reactsTo event

        when: 'the same user creation event is processed again'
        the user reactsTo event

        then: 'an error is thrown indicating the user is already created'
        thrown(RuntimeException)
    }

    def 'should throw an error when the request is not for the current user'() {
        given: 'a regular user in creation'
        def user = aUser()

        and: 'a command for another user'
        def command = anyValidUserCreateCommand()
        command['email'] = 'any-other@email.com'

        and: 'an event for different user'
        def inputEvent = anEvent() ofType USER_CREATED withPayload command

        when:
        the user reactsTo inputEvent

        then:
        def e = thrown(RuntimeException)
        e.message == "Responsible user doesn't have permissions to create the user"
    }
}
