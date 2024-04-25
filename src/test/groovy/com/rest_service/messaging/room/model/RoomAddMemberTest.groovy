package com.rest_service.messaging.room.model

import spock.lang.Specification

import static com.rest_service.Fixture.anyValidRoomAddMemberCommand
import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_MEMBER_ADDED
import static com.rest_service.messaging.room.model.RoomDSL.aRoom
import static com.rest_service.messaging.room.model.RoomDSL.the
import static com.rest_service.messaging.room.model.RoomDomainEventDSL.anEvent

class RoomAddMemberTest extends Specification {

    def 'should approve room add member event on successful initiate event'() {
        given: 'a created room'
        def room = aRoom()
        def roomCreatedEvent = anEvent() ofType ROOM_CREATED withPayload anyValidRoomCreateCommand()
        the room reactsTo roomCreatedEvent

        and: 'a room add member command'
        def newMemberId = UUID.randomUUID()
        def command = anyValidRoomAddMemberCommand()
        command['memberId'] = newMemberId

        and: 'a room add member event'
        def roomAddMemberEvent = anEvent() ofType ROOM_MEMBER_ADDED withPayload command

        when:
        the room reactsTo roomAddMemberEvent

        then:
        def roomData = (the room data())
        roomData.members.size() > 1
        roomData.members.contains(newMemberId)
    }

    def 'should throw an error when trying to approve the room add member for not yet created room'() {
        given: 'a room in InCreation state'
        def user = aRoom()

        and: 'request to approve the room creation'
        def roomCreatedEvent = anEvent() ofType ROOM_MEMBER_ADDED withPayload anyValidRoomAddMemberCommand()

        when:
        the user reactsTo roomCreatedEvent

        then:
        thrown(RuntimeException)
    }
}
