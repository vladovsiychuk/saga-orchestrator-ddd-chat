package com.rest_service.messaging.user.model

import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.Fixture.anyValidUserCreateCommand
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.ROOM_CREATE_APPROVED
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.rest_service.messaging.user.model.UserDomainDSL.aUser
import static com.rest_service.messaging.user.model.UserDomainDSL.the
import static com.rest_service.messaging.user.model.UserDomainEventDSL.anEvent

class RoomCreateTest extends Specification {

    def 'should approve room creation when responsible user just exists'() {
        given: 'an existing regular user'
        def user = aUser() withResponsibleUserEmail "example@test.com"
        def createdEvent = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()
        the user reactsTo createdEvent

        and: 'request to approve the room creation'
        def roomCreatedEvent = anEvent() ofType ROOM_CREATE_APPROVED withPayload anyValidRoomCreateCommand() from "example@test.com"

        when:
        the user reactsTo roomCreatedEvent

        then:
        (the user responseEvent() type) == SagaEventType.ROOM_CREATE_APPROVED
    }

    def 'should throw an error when trying to approve the room creation for not yet created user'() {
        given: 'a user in InCreation state'
        def user = aUser() withResponsibleUserEmail "example@test.com"

        and: 'request to approve the room creation'
        def roomCreatedEvent = anEvent() ofType ROOM_CREATE_APPROVED withPayload anyValidRoomCreateCommand() from "example@test.com"

        when:
        the user reactsTo roomCreatedEvent

        then:
        thrown(UnsupportedOperationException)
    }
}
