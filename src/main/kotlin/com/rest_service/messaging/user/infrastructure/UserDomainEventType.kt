package com.rest_service.messaging.user.infrastructure

enum class UserDomainEventType {
    USER_CREATED, UNDO,
    ROOM_CREATE_APPROVED,
    ROOM_ADD_MEMBER_APPROVED,
    MESSAGE_UPDATE_APPROVED,
    MESSAGE_READ_APPROVED,
    MESSAGE_TRANSLATE_APPROVED,
}
