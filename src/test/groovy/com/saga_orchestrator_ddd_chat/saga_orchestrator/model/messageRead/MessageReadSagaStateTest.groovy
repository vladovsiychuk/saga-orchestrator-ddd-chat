package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageRead

import spock.lang.Specification

import static com.saga_orchestrator_ddd_chat.Fixture.*
import static com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType.*
import static com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum.*
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.SagaEventDSL.anEvent
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageRead.MessageReadSagaDSL.aMessageReadSaga
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageRead.MessageReadSagaDSL.the

class MessageReadSagaStateTest extends Specification {

    def 'should change the status from READY to INITIATED on MESSAGE_READ_START event'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageReadSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidMessageReadCommand() ofType MESSAGE_READ_START

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_READ_INITIATED
    }

    def 'should change the status from INITIATED to COMPLETED on MESSAGE_READ_APPROVE event from USER_SERVICE, ROOM_SERVICE and MESSAGE_SERVICE'() {
        given: 'a user saga in INITIATED state'
        def userSaga = aMessageReadSaga()
        def readEvent = anEvent() from SAGA_SERVICE withPayload anyValidMessageReadCommand() ofType MESSAGE_READ_START
        the userSaga reactsTo readEvent.event

        and:
        def approvedEventFromUserService = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType MESSAGE_READ_APPROVED
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType MESSAGE_READ_APPROVED
        def approvedEventFromMessageService = anEvent() from MESSAGE_SERVICE withPayload anyValidMessageDTO() ofType MESSAGE_READ_APPROVED

        when:
        the userSaga reactsTo approvedEventFromUserService.event
        the userSaga reactsTo approvedEventFromRoomService.event
        the userSaga reactsTo approvedEventFromMessageService.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_READ_COMPLETED
    }

    def 'should change the status to ERROR when processing the REJECTED event'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageReadSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidErrorDto() ofType MESSAGE_READ_REJECTED

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_READ_ERROR
    }


    def 'should throw an exception when a not expected event is received'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageReadSaga()
        and:
        def event = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType USER_CREATE_APPROVED

        when:
        the userSaga reactsTo event.event

        then:
        thrown(UnsupportedOperationException)
    }
}
