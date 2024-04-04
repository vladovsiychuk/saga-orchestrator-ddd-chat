package com.rest_service.messaging.room.model

import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidRoomAddMemberCommand
import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_MEMBER_ADDED
import static com.rest_service.messaging.room.model.RoomDomainDSL.aRoom
import static com.rest_service.messaging.room.model.RoomDomainDSL.the
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
        (the room responseEvent() type) == SagaEventType.ROOM_ADD_MEMBER_APPROVED

        and: 'new member is added to the room'
        room.domain.room.members.size() > 1
        room.domain.room.members.contains(newMemberId)
    }
}
