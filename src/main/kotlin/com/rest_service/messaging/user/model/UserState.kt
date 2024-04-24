package com.rest_service.messaging.user.model

import com.rest_service.messaging.user.infrastructure.UserDomainEvent

interface UserState {
    fun apply(event: UserDomainEvent): UserDomainEvent
}
