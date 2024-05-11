package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.roomCreate

import spock.lang.Specification

import static com.saga_orchestrator_ddd_chat.Fixture.*
import static com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType.*
import static com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum.*
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.SagaEventDSL.anEvent
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.roomCreate.RoomCreateSagaDSL.aRoomSaga
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.roomCreate.RoomCreateSagaDSL.the

class RoomCreateSagaStateTest extends Specification {

    def 'should change the status from READY to INITIATED on ROOM_CREATE_START event'() {
        given: 'a room saga in READY state'
        def roomSaga = aRoomSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidRoomCreateCommand() ofType ROOM_CREATE_START

        when:
        the roomSaga reactsTo event.event

        then:
        (the roomSaga responseEvent() type) == ROOM_CREATE_INITIATED
    }

    def 'should change the status from INITIATED to COMPLETED on ROOM_CREATE_APPROVE event from ROOM_SERVICE and USER_SERVICE'() {
        given: 'a room saga in INITIATED state'
        def roomSaga = aRoomSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidRoomCreateCommand() ofType ROOM_CREATE_START
        the roomSaga reactsTo createEvent.event

        and:
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType ROOM_CREATE_APPROVED
        def approvedEventFromUserService = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType ROOM_CREATE_APPROVED

        when:
        the roomSaga reactsTo approvedEventFromRoomService.event
        the roomSaga reactsTo approvedEventFromUserService.event

        then:
        (the roomSaga responseEvent() type) == ROOM_CREATE_COMPLETED
    }

    def 'should not have a response event when not all approved events were received'() {
        given: 'a room saga in INITIATED state'
        def roomSaga = aRoomSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidRoomCreateCommand() ofType ROOM_CREATE_START
        the roomSaga reactsTo createEvent.event

        and: 'only one service approved event'
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType ROOM_CREATE_APPROVED

        when:
        the roomSaga reactsTo approvedEventFromRoomService.event

        then:
        (the roomSaga responseEvent()) == null
    }

    def 'should change the status to ERROR when processing the REJECTED event'() {
        given: 'a room saga in READY state'
        def roomSaga = aRoomSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidErrorDto() ofType ROOM_CREATE_REJECTED

        when:
        the roomSaga reactsTo event.event

        then:
        (the roomSaga responseEvent() type) == ROOM_CREATE_ERROR
    }

    def 'should throw an exception when a not expected event is received in READY state'() {
        given: 'a room saga in READY state'
        def roomSaga = aRoomSaga()
        and:
        def event = anEvent() from USER_SERVICE withPayload anyValidRoomDTO() ofType ROOM_CREATE_APPROVED

        when:
        the roomSaga reactsTo event.event

        then:
        thrown(UnsupportedOperationException)
    }

    def 'should throw an exception when a not expected event is received in INITIATED state'() {
        given: 'a room saga in INITIATED state'
        def roomSaga = aRoomSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidRoomCreateCommand() ofType ROOM_CREATE_START
        the roomSaga reactsTo createEvent.event

        and: 'not expected event'
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType ROOM_CREATE_START

        when:
        the roomSaga reactsTo approvedEventFromRoomService.event

        then:
        thrown(UnsupportedOperationException)
    }
}
