package com.rest_service.messaging.user.infrastructure

enum class UserDomainEventType {
    USER_CREATED, UNDO,
    ROOM_CREATE_APPROVED,
    ROOM_ADD_MEMBER_APPROVED,
}
