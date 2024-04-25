package com.rest_service.messaging.room.model

import spock.lang.Specification

import static RoomDSL.aRoom
import static RoomDSL.the
import static com.rest_service.Fixture.anyValidMessageCreateCommand
import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.MESSAGE_CREATE_APPROVED
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.rest_service.messaging.room.model.RoomDomainEventDSL.anEvent

class MessageCreateTest extends Specification {

    def 'should approve message creation when the room just exists'() {
        given: 'a created room'
        def room = aRoom()
        def roomCreatedEvent = anEvent() ofType ROOM_CREATED withPayload anyValidRoomCreateCommand()
        the room reactsTo roomCreatedEvent

        and: 'a message create command'
        def messageCreateApprovedEvent = anEvent() ofType MESSAGE_CREATE_APPROVED withPayload anyValidMessageCreateCommand()

        when:
        the room reactsTo messageCreateApprovedEvent

        then:
        (the room data()) != null
    }

    def 'should throw an error when trying to approve message creation on non-existing room'() {
        given: 'a room in InCreation state'
        def room = aRoom()

        and: 'a message create command'
        def messageCreateEvent = anEvent() ofType MESSAGE_CREATE_APPROVED withPayload anyValidMessageCreateCommand()

        when:
        the room reactsTo messageCreateEvent

        then:
        thrown(RuntimeException)
    }
}
