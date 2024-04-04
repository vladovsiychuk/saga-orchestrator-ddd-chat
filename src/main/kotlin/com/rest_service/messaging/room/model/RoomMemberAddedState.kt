package com.rest_service.messaging.room.model

import com.rest_service.commons.SagaEvent
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import reactor.kotlin.core.publisher.toMono

class RoomMemberAddedState(private val domain: RoomDomain) : RoomState {
    override fun createResponseEvent() = SagaEvent(SagaEventType.ROOM_ADD_MEMBER_APPROVED, domain.operationId, ServiceEnum.USER_SERVICE, domain.responsibleUserEmail, domain.responsibleUserId, domain.room!!).toMono()
    override fun apply(event: RoomDomainEvent) = throw UnsupportedOperationException()
}
