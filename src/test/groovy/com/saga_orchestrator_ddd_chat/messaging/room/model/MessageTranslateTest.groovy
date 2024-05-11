package com.saga_orchestrator_ddd_chat.messaging.room.model


import spock.lang.Specification

import static RoomDSL.aRoom
import static RoomDSL.the
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidMessageTranslateCommand
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidRoomCreateCommand
import static com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEventType.MESSAGE_TRANSLATE_APPROVED
import static com.saga_orchestrator_ddd_chat.messaging.room.infrastructure.RoomDomainEventType.ROOM_CREATED
import static com.saga_orchestrator_ddd_chat.messaging.room.model.RoomDomainEventDSL.anEvent

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
        (the room data()) != null
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

    def 'should throw an error when trying to approve the translation on non-existing room'() {
        given: 'a room in InCreation state'
        def room = aRoom()

        and: 'a message translate command'
        def messageTranslateEvent = anEvent() ofType MESSAGE_TRANSLATE_APPROVED withPayload anyValidMessageTranslateCommand()

        when:
        the room reactsTo messageTranslateEvent

        then:
        thrown(RuntimeException)
    }
}
