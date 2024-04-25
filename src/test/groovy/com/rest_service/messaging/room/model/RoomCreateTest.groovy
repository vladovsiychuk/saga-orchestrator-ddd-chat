package com.rest_service.messaging.room.model

import spock.lang.Specification

import static RoomDSL.aRoom
import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.rest_service.messaging.room.model.RoomDSL.the
import static com.rest_service.messaging.room.model.RoomDomainEventDSL.anEvent

class RoomCreateTest extends Specification {

    def 'should approve room creation on successful initiate event'() {
        given:
        def room = aRoom()

        and: 'a room create event'
        def roomCreatedEvent = anEvent() ofType ROOM_CREATED withPayload anyValidRoomCreateCommand()

        when:
        the room reactsTo roomCreatedEvent

        then:
        (the room data()) != null
    }
}
