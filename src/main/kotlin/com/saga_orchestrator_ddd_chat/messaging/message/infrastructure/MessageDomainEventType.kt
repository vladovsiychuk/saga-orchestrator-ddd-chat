package com.saga_orchestrator_ddd_chat.messaging.message.infrastructure

enum class MessageDomainEventType {
    MESSAGE_CREATED,
    MESSAGE_UPDATED,
    MESSAGE_READ,
    MESSAGE_TRANSLATED,
    UNDO
}
