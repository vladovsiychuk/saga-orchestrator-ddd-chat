package com.rest_service.messaging.message.infrastructure

enum class MessageDomainEventType {
    MESSAGE_CREATED,
    MESSAGE_UPDATED,
    MESSAGE_READ,
    MESSAGE_TRANSLATED,
    UNDO
}
