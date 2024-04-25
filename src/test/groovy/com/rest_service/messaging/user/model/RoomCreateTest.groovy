package com.rest_service.messaging.user.model


import spock.lang.Specification

import static UserDSL.aUser
import static UserDSL.the
import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.Fixture.anyValidUserCreateCommand
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.ROOM_CREATE_APPROVED
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.rest_service.messaging.user.model.UserDomainEventDSL.anEvent

class RoomCreateTest extends Specification {

    def 'should approve room creation when the responsible user exists'() {
        given: 'an existing regular user'
        def user = aUser()
        def createdEvent = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()
        the user reactsTo createdEvent

        and: 'a request for approval to create a room'
        def roomCreatedEvent = anEvent() ofType ROOM_CREATE_APPROVED withPayload anyValidRoomCreateCommand()

        when:
        the user reactsTo roomCreatedEvent

        then:
        (the user data()) != null
    }

    def 'should throw an error when trying to approve the room creation for not yet created user'() {
        given: 'a user in InCreation state'
        def user = aUser()

        and: 'request to approve the room creation'
        def roomCreatedEvent = anEvent() ofType ROOM_CREATE_APPROVED withPayload anyValidRoomCreateCommand()

        when:
        the user reactsTo roomCreatedEvent

        then:
        thrown(RuntimeException)
    }
}
