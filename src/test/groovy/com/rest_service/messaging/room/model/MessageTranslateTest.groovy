package com.rest_service.messaging.room.model

import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidMessageTranslateCommand
import static com.rest_service.Fixture.anyValidRoomCreateCommand
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.MESSAGE_TRANSLATE_APPROVED
import static com.rest_service.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.rest_service.messaging.room.model.RoomDomainDSL.aRoom
import static com.rest_service.messaging.room.model.RoomDomainDSL.the
import static com.rest_service.messaging.room.model.RoomDomainEventDSL.anEvent

class MessageTranslateTest extends Specification {

    def 'should approve message translate when the responsible user is a room member'() {
        given: 'a created room from a specific user'
        def currentUserId = UUID.randomUUID()
        def room = aRoom()
        def roomCreatedEvent = anEvent() from currentUserId ofType ROOM_CREATED withPayload anyValidRoomCreateCommand()
        the room reactsTo roomCreatedEvent

        and: 'a message translate command from the same user'
        def messageTranslateApprovedEvent = anEvent() from currentUserId ofType MESSAGE_TRANSLATE_APPROVED withPayload anyValidMessageTranslateCommand()

        when:
        the room reactsTo messageTranslateApprovedEvent

        then:
        (the room responseEvent() type) == SagaEventType.MESSAGE_TRANSLATE_APPROVED
    }

    def 'should throw an error when a non-room member tries to translate the message'() {
        given: 'a created room from a specific user'
        def currentUserId = UUID.randomUUID()
        def room = aRoom()
        def roomCreatedEvent = anEvent() from currentUserId ofType ROOM_CREATED withPayload anyValidRoomCreateCommand()
        the room reactsTo roomCreatedEvent

        and: 'a message translate command from another user'
        def randomUserId = UUID.randomUUID()
        def messageTranslateApprovedEvent = anEvent() from randomUserId ofType MESSAGE_TRANSLATE_APPROVED withPayload anyValidMessageTranslateCommand()

        when:
        the room reactsTo messageTranslateApprovedEvent

        then:
        thrown(RuntimeException)
    }
}
