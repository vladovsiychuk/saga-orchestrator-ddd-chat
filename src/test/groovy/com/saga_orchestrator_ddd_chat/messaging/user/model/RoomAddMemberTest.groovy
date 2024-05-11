package com.saga_orchestrator_ddd_chat.messaging.user.model


import spock.lang.Specification

import static UserDSL.aUser
import static UserDSL.the
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidRoomAddMemberCommand
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidUserCreateCommand
import static com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventType.ROOM_ADD_MEMBER_APPROVED
import static com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.saga_orchestrator_ddd_chat.messaging.user.model.UserDomainEventDSL.anEvent

class RoomAddMemberTest extends Specification {

    def 'should approve room add member event when the responsible user exists'() {
        given: 'an existing regular user'
        def user = aUser()
        def createdEvent = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()
        the user reactsTo createdEvent

        and: 'request to approve the room creation'
        def roomAddMemberEvent = anEvent() ofType ROOM_ADD_MEMBER_APPROVED withPayload anyValidRoomAddMemberCommand()

        when:
        the user reactsTo roomAddMemberEvent

        then:
        (the user data()) != null
    }

    def 'should throw an error when trying to approve the room add member for not yet created user'() {
        given: 'a user in InCreation state'
        def user = aUser()

        and: 'request to approve the room creation'
        def roomCreatedEvent = anEvent() ofType ROOM_ADD_MEMBER_APPROVED withPayload anyValidRoomAddMemberCommand()

        when:
        the user reactsTo roomCreatedEvent

        then:
        thrown(RuntimeException)
    }
}
