package com.rest_service.messaging.user.model

import com.rest_service.messaging.user.infrastructure.UserDomainEvent

class MessageUpdateApprovedState(private val domain: UserDomain) : UserState {
    override fun apply(event: UserDomainEvent) = run {
        UserCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
