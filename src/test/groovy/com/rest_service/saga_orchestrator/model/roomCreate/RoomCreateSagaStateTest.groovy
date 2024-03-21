package com.rest_service.saga_orchestrator.model.roomCreate

import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.model.SagaStatus
import spock.lang.Specification

import static RoomCreateSagaStateDSL.the
import static com.rest_service.Fixture.anyValidRoomCommand
import static com.rest_service.Fixture.anyValidRoomDTO
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
        initialStatus        | eventType                     | expectedNewStatus    | expectedNextEventType          | responsibleService       | payload
        SagaStatus.READY     | EventType.ROOM_CREATE_START   | SagaStatus.INITIATED | EventType.ROOM_CREATE_INITIATE | ServiceEnum.SAGA_SERVICE | anyValidRoomCommand()
        SagaStatus.INITIATED | EventType.ROOM_CREATE_APPROVE | SagaStatus.COMPLETED | EventType.ROOM_CREATE_COMPLETE | ServiceEnum.ROOM_SERVICE | anyValidRoomDTO()
    }

    def 'should throw an error when an incorrect status change occurs'() {
        given:
        the state withStatus SagaStatus.READY
        and:
        def event = anEvent() with anyValidRoomDTO() ofType EventType.ROOM_CREATE_APPROVE

        when:
        the state reactsTo event.event

        then:
        def e = thrown(RuntimeException)
        e.message == "Status cannot be changed from READY to IN_APPROVING"
    }
}
