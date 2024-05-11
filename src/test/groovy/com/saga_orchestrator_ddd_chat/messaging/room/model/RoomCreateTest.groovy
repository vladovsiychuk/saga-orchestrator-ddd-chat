package com.saga_orchestrator_ddd_chat.messaging.room.model

import spock.lang.Specification

import static RoomDSL.aRoom
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidRoomCreateCommand
import static com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.saga_orchestrator_ddd_chat.messaging.room.model.RoomDSL.the
import static com.saga_orchestrator_ddd_chat.messaging.room.model.RoomDomainEventDSL.anEvent

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
