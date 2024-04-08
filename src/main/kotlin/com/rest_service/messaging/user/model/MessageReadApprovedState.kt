package com.rest_service.messaging.user.model

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import reactor.kotlin.core.publisher.toMono

class MessageReadApprovedState(private val domain: UserDomain) : UserState {
    override fun createResponseEvent() = SagaEvent(SagaEventType.MESSAGE_READ_APPROVED, domain.operationId, ServiceEnum.USER_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId!!, domain.currentUser!!).toMono()
    override fun apply(event: UserDomainEvent) = run {
        UserCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
