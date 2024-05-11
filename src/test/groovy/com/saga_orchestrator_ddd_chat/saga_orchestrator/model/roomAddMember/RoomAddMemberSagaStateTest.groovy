package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.roomAddMember

import spock.lang.Specification

import static com.saga_orchestrator_ddd_chat.Fixture.*
import static com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType.*
import static com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum.*
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.SagaEventDSL.anEvent
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.roomAddMember.RoomAddMemberSagaDSL.aRoomAddMemberSaga
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.roomAddMember.RoomAddMemberSagaDSL.the

class RoomAddMemberSagaStateTest extends Specification {

    def 'should change the status from READY to INITIATED on ROOM_ADD_MEMBER_START event'() {
        given: 'a room saga in READY state'
        def roomAddMemberSaga = aRoomAddMemberSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidRoomAddMemberCommand() ofType ROOM_ADD_MEMBER_START

        when:
        the roomAddMemberSaga reactsTo event.event

        then:
        (the roomAddMemberSaga responseEvent() type) == ROOM_ADD_MEMBER_INITIATED
    }

    def 'should change the status from INITIATED to COMPLETED on ROOM_ADD_MEMBER_APPROVE event from ROOM_SERVICE and USER_SERVICE'() {
        given: 'a room add member saga in INITIATED state'
        def roomSaga = aRoomAddMemberSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidRoomAddMemberCommand() ofType ROOM_ADD_MEMBER_START
        the roomSaga reactsTo createEvent.event

        and:
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType ROOM_ADD_MEMBER_APPROVED
        def approvedEventFromUserService = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType ROOM_ADD_MEMBER_APPROVED

        when:
        the roomSaga reactsTo approvedEventFromRoomService.event
        the roomSaga reactsTo approvedEventFromUserService.event

        then:
        (the roomSaga responseEvent() type) == ROOM_ADD_MEMBER_COMPLETED
    }

    def 'should not have a response event when not all approved events were received'() {
        given: 'a room add member saga in INITIATED state'
        def roomSaga = aRoomAddMemberSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidRoomAddMemberCommand() ofType ROOM_ADD_MEMBER_START
        the roomSaga reactsTo createEvent.event

        and: 'only one service approved event'
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType ROOM_ADD_MEMBER_APPROVED

        when:
        the roomSaga reactsTo approvedEventFromRoomService.event

        then:
        (the roomSaga responseEvent()) == null
    }

    def 'should change the status to ERROR when processing REJECTED event'() {
        given: 'a room saga in READY state'
        def roomAddMemberSaga = aRoomAddMemberSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidErrorDto() ofType ROOM_ADD_MEMBER_REJECTED

        when:
        the roomAddMemberSaga reactsTo event.event

        then:
        (the roomAddMemberSaga responseEvent() type) == ROOM_ADD_MEMBER_ERROR
    }

    def 'should throw an exception when a not expected event is received in READY state'() {
        given: 'a room add member saga in READY state'
        def roomSaga = aRoomAddMemberSaga()
        and:
        def event = anEvent() from USER_SERVICE withPayload anyValidRoomDTO() ofType ROOM_CREATE_START

        when:
        the roomSaga reactsTo event.event

        then:
        thrown(UnsupportedOperationException)
    }

    def 'should throw an exception when a not expected event is received in INITIATED state'() {
        given: 'a room saga in INITIATED state'
        def roomSaga = aRoomAddMemberSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidRoomAddMemberCommand() ofType ROOM_ADD_MEMBER_START
        the roomSaga reactsTo createEvent.event

        and: 'not expected event'
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType ROOM_CREATE_START

        when:
        the roomSaga reactsTo approvedEventFromRoomService.event

        then:
        thrown(UnsupportedOperationException)
    }
}
