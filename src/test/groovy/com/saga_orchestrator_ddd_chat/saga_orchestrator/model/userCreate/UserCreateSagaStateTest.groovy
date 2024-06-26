package com.saga_orchestrator_ddd_chat.saga_orchestrator.model.userCreate

import spock.lang.Specification

import static com.saga_orchestrator_ddd_chat.Fixture.*
import static com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType.*
import static com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum.SAGA_SERVICE
import static com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum.USER_SERVICE
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.SagaEventDSL.anEvent
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.userCreate.UserCreateSagaDSL.aUserSaga
import static com.saga_orchestrator_ddd_chat.saga_orchestrator.model.userCreate.UserCreateSagaDSL.the

class UserCreateSagaStateTest extends Specification {

    def 'should change the status from READY to INITIATED on USER_CREATE_START event'() {
        given: 'a user saga in READY state'
        def userSaga = aUserSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidUserCreateCommand() ofType USER_CREATE_START

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == USER_CREATE_INITIATED
    }

    def 'should change the status from INITIATED to COMPLETED on USER_CREATE_APPROVE event from USER_SERVICE'() {
        given: 'a user saga in INITIATED state'
        def userSaga = aUserSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidUserCreateCommand() ofType USER_CREATE_START
        the userSaga reactsTo createEvent.event

        and:
        def approvedEventFromUserService = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType USER_CREATE_APPROVED

        when:
        the userSaga reactsTo approvedEventFromUserService.event

        then:
        (the userSaga responseEvent() type) == USER_CREATE_COMPLETED
    }

    def 'should change the status to ERROR when processing the REJECTED event'() {
        given: 'a user saga in READY state'
        def userSaga = aUserSaga()
        and:
        def event = anEvent() from SAGA_SERVICE withPayload anyValidErrorDto() ofType USER_CREATE_REJECTED

        when:
        the userSaga reactsTo event.event

        then:
        (the userSaga responseEvent() type) == USER_CREATE_ERROR
    }


    def 'should throw an exception when a not expected event is received in READY state'() {
        given: 'a user saga in READY state'
        def userSaga = aUserSaga()
        and:
        def event = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType USER_CREATE_APPROVED

        when:
        the userSaga reactsTo event.event

        then:
        thrown(UnsupportedOperationException)
    }

    def 'should throw an exception when a not expected event is received in INITIATED state'() {
        given: 'a user saga in INITIATED state'
        def userSaga = aUserSaga()
        def createEvent = anEvent() from SAGA_SERVICE withPayload anyValidUserCreateCommand() ofType USER_CREATE_START
        the userSaga reactsTo createEvent.event

        and: 'not expected event'
        def approvedEventFromUserService = anEvent() from USER_SERVICE withPayload anyValidUserDTO() ofType USER_CREATE_START

        when:
        the userSaga reactsTo approvedEventFromUserService.event

        then:
        thrown(UnsupportedOperationException)
    }
}
