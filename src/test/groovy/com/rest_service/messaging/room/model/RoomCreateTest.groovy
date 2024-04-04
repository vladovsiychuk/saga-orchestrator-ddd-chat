package com.rest_service.messaging.room.model

import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.rest_service.messaging.room.model.RoomDomainDSL.aRoom
import static com.rest_service.messaging.room.model.RoomDomainDSL.the
import static com.rest_service.messaging.room.model.RoomDomainEventDSL.anEvent

class RoomCreateTest extends Specification {

    def 'should approve room creation on successful initiate event'() {
        given:
        def room = aRoom()

        and: 'a user create event'
        def roomCreatedEvent = anEvent() ofType ROOM_CREATED withPayload anyValidRoomCreateCommand()

        when:
        the room reactsTo roomCreatedEvent

        then:
        (the room responseEvent() type) == SagaEventType.ROOM_CREATE_APPROVED
    }
}
