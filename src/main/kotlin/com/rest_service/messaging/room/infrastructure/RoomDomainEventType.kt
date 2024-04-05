package com.rest_service.messaging.room.infrastructure

enum class RoomDomainEventType {
    ROOM_CREATED,
    ROOM_MEMBER_ADDED,
    MESSAGE_CREATE_APPROVED,
    UNDO
}
