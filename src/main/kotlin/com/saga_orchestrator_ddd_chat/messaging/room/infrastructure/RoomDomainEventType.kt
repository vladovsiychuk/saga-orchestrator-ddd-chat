package com.saga_orchestrator_ddd_chat.messaging.room.infrastructure

enum class RoomDomainEventType {
    ROOM_CREATED,
    ROOM_MEMBER_ADDED,
    MESSAGE_CREATE_APPROVED,
    MESSAGE_READ_APPROVED,
    MESSAGE_TRANSLATE_APPROVED,
    UNDO
}
