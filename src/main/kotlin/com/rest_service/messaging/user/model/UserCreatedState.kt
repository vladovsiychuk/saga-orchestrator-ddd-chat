package com.rest_service.messaging.user.model

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import reactor.kotlin.core.publisher.toMono

class UserCreatedState(private val domain: UserDomain) : UserState {

    override fun apply(event: UserDomainEvent): UserDomainEvent {
        return when (event.type) {
            UserDomainEventType.ROOM_CREATE_APPROVED -> approveRoomCreate(event)
            else                                     -> throw UnsupportedOperationException()
        }
    }

    private fun approveRoomCreate(event: UserDomainEvent): UserDomainEvent {
        domain.changeState(RoomCreateApprovedState(domain))
        return event
    }

    override fun createResponseEvent() = SagaEvent(SagaEventType.USER_CREATE_APPROVE, domain.operationId, ServiceEnum.USER_SERVICE, domain.responsibleUserEmail, domain.currentUser!!.id, domain.currentUser!!).toMono()
}
