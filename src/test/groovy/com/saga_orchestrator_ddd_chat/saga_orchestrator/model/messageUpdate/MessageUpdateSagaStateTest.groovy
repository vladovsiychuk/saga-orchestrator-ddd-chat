package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageUpdate

import spock.lang.Specification

import static com.saga_orchestrator_ddd_chat.Fixture.*
import static com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType.*
import static com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum.*
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.SagaEventDSL.anEvent
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageUpdate.MessageUpdateSagaDSL.aMessageUpdateSaga
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.messageUpdate.MessageUpdateSagaDSL.the

class MessageUpdateSagaStateTest extends Specification {

    def 'should change the status from READY to INITIATED on MESSAGE_UPDATE_START event'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageUpdateSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidMessageUpdateCommand() ofType MESSAGE_UPDATE_START

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_UPDATE_INITIATED
    }

    def 'should change the status from INITIATED to COMPLETED on MESSAGE_UPDATE_APPROVE event from USER_SERVICE and MESSAGE_SERVICE'() {
        given: 'a user saga in INITIATED state'
        def userSaga = aMessageUpdateSaga()
        def updateEvent = anEvent() from SAGA_SERVICE withPayload anyValidMessageUpdateCommand() ofType MESSAGE_UPDATE_START
        the userSaga reactsTo updateEvent.event

        and:
        def approvedEventFromUserService = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType MESSAGE_UPDATE_APPROVED
        def approvedEventFromMessageService = anEvent() from MESSAGE_SERVICE withPayload anyValidMessageDTO() ofType MESSAGE_UPDATE_APPROVED

        when:
        the userSaga reactsTo approvedEventFromUserService.event
        the userSaga reactsTo approvedEventFromMessageService.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_UPDATE_COMPLETED
    }

    def 'should change the status to ERROR when processing the REJECTED event'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageUpdateSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidErrorDto() ofType MESSAGE_UPDATE_REJECTED

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == MESSAGE_UPDATE_ERROR
    }


    def 'should throw an exception when a not expected event is received'() {
        given: 'a user saga in READY state'
        def userSaga = aMessageUpdateSaga()
        and:
        def event = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType USER_CREATE_APPROVED

        when:
        the userSaga reactsTo event.event

        then:
        thrown(UnsupportedOperationException)
    }
}
