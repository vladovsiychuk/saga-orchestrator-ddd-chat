package com.rest_service.saga_orchestrator.model.userCreate

import com.rest_service.commons.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.model.UserCreateSaga

class UserCreateSagaDSL {
    UserCreateSaga state = new UserCreateSaga(UUID.randomUUID(), "example@test.com")

    static UserCreateSagaDSL the(UserCreateSagaDSL dsl) {
        return dsl
    }

    static UserCreateSagaDSL aUserSaga() {
        return new UserCreateSagaDSL()
    }


    UserCreateSagaDSL reactsTo(SagaDomainEvent event) {
        state.apply event
        return this
    }

    SagaEvent responseEvent() {
        state.createResponseSagaEvent().block()
    }
}
