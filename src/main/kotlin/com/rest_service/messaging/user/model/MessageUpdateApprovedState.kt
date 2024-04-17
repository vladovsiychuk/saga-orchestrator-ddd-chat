package com.rest_service.messaging.user.model

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import reactor.kotlin.core.publisher.toMono

class MessageUpdateApprovedState(private val domain: UserDomain) : UserState {
    override fun createResponseEvent(sagaEvent: SagaEvent) = SagaEvent(SagaEventType.MESSAGE_UPDATE_APPROVED, domain.operationId, ServiceEnum.USER_SERVICE, sagaEvent.responsibleUserId, domain.currentUser).toMono()
    override fun apply(event: UserDomainEvent) = run {
        UserCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
