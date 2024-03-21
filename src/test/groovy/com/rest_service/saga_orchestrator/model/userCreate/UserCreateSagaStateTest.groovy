package com.rest_service.saga_orchestrator.model.userCreate

import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.model.SagaStatus
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidUserCommand
import static com.rest_service.Fixture.anyValidUserDTO
import static com.rest_service.saga_orchestrator.model.SagaEventDSL.anEvent
import static com.rest_service.saga_orchestrator.model.userCreate.UserCreateSagaStateDSL.the

class UserCreateSagaStateTest extends Specification {

    UserCreateSagaStateDSL state

    def setup() {
        EventFactory eventFactory = Mock()
        state = new UserCreateSagaStateDSL(eventFactory)
    }

    def 'should change the status from #initialStatus to #expectedNewStatus on #eventType event'() {
        given:
        the state withStatus initialStatus
        and:
        def event = anEvent() from responsibleService withPayload payload ofType eventType

        when:
        the state reactsTo event.event

        then:
        the state hasStatus expectedNewStatus
        and:
        (the state nextEvent() type) == expectedNextEventType

        where:
        initialStatus        | eventType                     | expectedNewStatus    | expectedNextEventType          | responsibleService       | payload
        SagaStatus.READY     | EventType.USER_CREATE_START   | SagaStatus.INITIATED | EventType.USER_CREATE_INITIATE | ServiceEnum.SAGA_SERVICE | anyValidUserCommand()
        SagaStatus.INITIATED | EventType.USER_CREATE_APPROVE | SagaStatus.COMPLETED | EventType.USER_CREATE_COMPLETE | ServiceEnum.USER_SERVICE | anyValidUserDTO()
    }

    def 'should throw an error when an incorrect status change occurs'() {
        given:
        the state withStatus SagaStatus.READY
        and:
        def event = anEvent() with anyValidUserDTO() ofType EventType.USER_CREATE_APPROVE

        when:
        the state reactsTo event.event

        then:
        def e = thrown(RuntimeException)
        e.message == "Status cannot be changed from READY to IN_APPROVING"
    }
}
