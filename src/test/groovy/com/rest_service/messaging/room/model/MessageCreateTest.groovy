package com.rest_service.messaging.room.model

import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidMessageCreateCommand
import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.MESSAGE_CREATE_APPROVED
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.rest_service.messaging.room.model.RoomDomainDSL.aRoom
import static com.rest_service.messaging.room.model.RoomDomainDSL.the
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
        (the room responseEvent() type) == SagaEventType.MESSAGE_CREATE_APPROVED
    }
}
