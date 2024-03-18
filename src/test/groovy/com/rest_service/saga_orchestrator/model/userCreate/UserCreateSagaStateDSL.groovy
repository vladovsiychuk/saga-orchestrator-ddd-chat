package com.rest_service.saga_orchestrator.model.userCreate

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.enums.SagaType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import com.rest_service.saga_orchestrator.model.SagaStatus
import com.rest_service.saga_orchestrator.model.UserCreateSagaState

import static com.rest_service.saga_orchestrator.model.SagaEventDSL.anEvent

class UserCreateSagaStateDSL {
    UserCreateSagaState state

    static UserCreateSagaStateDSL the(UserCreateSagaStateDSL dsl) {
        return dsl
    }

    UserCreateSagaStateDSL(EventFactory eventFactory) {
        state = new UserCreateSagaState(UUID.randomUUID(), eventFactory)
    }

    UserCreateSagaStateDSL withStatus(SagaStatus status) {
        switch (status) {
            case SagaStatus.READY:
                // No additional actions required.
                break
            case SagaStatus.INITIATED:
                state.apply(anEvent() withAnyValidUserCommand() and() ofType SagaType.USER_CREATE_START event)
                break
            case SagaStatus.COMPLETED:
                state.apply(anEvent() from ServiceEnum.USER_SERVICE withAnyValidUserDTO() and() ofType SagaType.USER_CREATE_APPROVE event)
                break
        }

        return this
    }

    UserCreateSagaStateDSL reactsTo(SagaEvent event) {
        state.apply event
        return this
    }

    DomainEvent nextEvent() {
        state.createNextEvent().block()
    }

    Boolean hasStatus(SagaStatus status) {
        return state.status == status
    }
}
