package com.rest_service.saga_orchestrator.model.messageCreate

import spock.lang.Specification

import static com.rest_service.Fixture.*
import static com.rest_service.commons.enums.SagaEventType.*
import static com.rest_service.commons.enums.ServiceEnum.*
import static com.rest_service.saga_orchestrator.model.SagaEventDSL.anEvent
import static com.rest_service.saga_orchestrator.model.messageCreate.MessageCreateSagaDSL.aMessageSaga
import static com.rest_service.saga_orchestrator.model.messageCreate.MessageCreateSagaDSL.the

class MessageCreateSagaStateTest extends Specification {

    def 'should change the status from READY to INITIATED on MESSAGE_CREATE_START event'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidMessageCreateCommand() ofType MESSAGE_CREATE_START

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_CREATE_INITIATED
    }

    def 'should change the status from INITIATED to COMPLETED on MESSAGE_CREATE_APPROVE event from both ROOM_SERVICE and MESSAGE_SERVICE'() {
        given: 'a user saga in INITIATED state'
        def userSaga = aMessageSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidMessageCreateCommand() ofType MESSAGE_CREATE_START
        the userSaga reactsTo createEvent.event

        and:
        def approvedEventFromRoomService = anEvent() from ROOM_SERVICE withPayload anyValidRoomDTO() ofType MESSAGE_CREATE_APPROVED
        def approvedEventFromMessageService = anEvent() from MESSAGE_SERVICE withPayload anyValidMessageDTO() ofType MESSAGE_CREATE_APPROVED

        when:
        the userSaga reactsTo approvedEventFromRoomService.event
        the userSaga reactsTo approvedEventFromMessageService.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_CREATE_COMPLETED
    }

    def 'should change the status to ERROR when processing the REJECTED event'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidErrorDto() ofType MESSAGE_CREATE_REJECTED

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_CREATE_ERROR
    }


    def 'should throw an exception when a not expected event is received'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageSaga()
        and:
        def event = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType USER_CREATE_APPROVED

        when:
        the userSaga reactsTo event.event

        then:
        thrown(UnsupportedOperationException)
    }
}
