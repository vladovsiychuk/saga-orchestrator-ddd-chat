package com.rest_service.messaging.room.model


import spock.lang.Specification

import static RoomDSL.aRoom
import static RoomDSL.the
import static com.rest_service.Fixture.anyValidMessageReadCommand
import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.MESSAGE_READ_APPROVED
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.rest_service.messaging.room.model.RoomDomainEventDSL.anEvent

class MessageReadTest extends Specification {

    def 'should approve message read when the responsible user is a room member'() {
        given: 'a created room from a specific user'
        def currentUserId = UUID.randomUUID()
        def room = aRoom()
        def roomCreatedEvent = anEvent() from currentUserId ofType ROOM_CREATED withPayload anyValidRoomCreateCommand()
        the room reactsTo roomCreatedEvent

        and: 'a message read command from the same user'
        def messageReadApprovedEvent = anEvent() from currentUserId ofType MESSAGE_READ_APPROVED withPayload anyValidMessageReadCommand()

        when:
        the room reactsTo messageReadApprovedEvent

        then:
        (the room data()) != null
    }

    def 'should throw an error when a non-room member tries to read the message'() {
        given: 'a created room from a specific user'
        def currentUserId = UUID.randomUUID()
        def room = aRoom()
        def roomCreatedEvent = anEvent() from currentUserId ofType ROOM_CREATED withPayload anyValidRoomCreateCommand()
        the room reactsTo roomCreatedEvent

        and: 'a message read command from another user'
        def randomUserId = UUID.randomUUID()
        def messageReadApprovedEvent = anEvent() from randomUserId ofType MESSAGE_READ_APPROVED withPayload anyValidMessageReadCommand()

        when:
        the room reactsTo messageReadApprovedEvent

        then:
        thrown(RuntimeException)
    }

    def 'should throw an error when trying to approve the read on non-existing room'() {
        given: 'a room in InCreation state'
        def room = aRoom()

        and: 'a message read command'
        def messageReadEvent = anEvent() ofType MESSAGE_READ_APPROVED withPayload anyValidMessageReadCommand()

        when:
        the room reactsTo messageReadEvent

        then:
        thrown(RuntimeException)
    }
}
