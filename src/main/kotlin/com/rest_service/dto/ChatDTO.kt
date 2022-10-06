package com.rest_service.dto

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class ChatDTO(
    val companion: UUID,
    val messages: List<MessageDTO>,
)
