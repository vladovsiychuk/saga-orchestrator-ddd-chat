package com.rest_service.saga_orchestrator.model

import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import spock.lang.Specification

import static com.rest_service.saga_orchestrator.model.RoomCreateSagaStateDSL.the
import static com.rest_service.saga_orchestrator.model.SagaEventDSL.anEvent

class RoomCreateSagaStateTest extends Specification {

    RoomCreateSagaStateDSL state

    def setup() {
        EventFactory eventFactory = Mock()
        state = new RoomCreateSagaStateDSL(eventFactory)
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
        initialStatus        | eventType                    | expectedNewStatus    | expectedNextEventType         | responsibleService       | payload
        SagaStatus.READY     | SagaType.ROOM_CREATE_START   | SagaStatus.INITIATED | SagaType.ROOM_CREATE_INITIATE | ServiceEnum.SAGA_SERVICE | ["userId": UUID.randomUUID()]
        SagaStatus.INITIATED | SagaType.ROOM_CREATE_APPROVE | SagaStatus.COMPLETED | SagaType.ROOM_CREATE_COMPLETE | ServiceEnum.ROOM_SERVICE | ["id": UUID.randomUUID(), "createdBy": UUID.randomUUID(), "members": [], "dateCreated": 123, "dateUpdated": 123]
    }
}
