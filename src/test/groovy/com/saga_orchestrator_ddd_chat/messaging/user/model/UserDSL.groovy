package com.saga_orchestrator_ddd_chat.messaging.user.model

import com.saga_orchestrator_ddd_chat.commons.dto.UserDTO

class UserDSL {
    User domain = new User()

    static UserDSL aUser() {
        return new UserDSL()
    }

    static UserDSL the(UserDSL dsl) {
        return dsl
    }

    UserDSL and() {
        return this
    }

    UserDSL reactsTo(UserDomainEventDSL event) {
        domain.apply(event.event)
        return this
    }

    UserDTO data() {
        return domain.toDto()
    }
}

