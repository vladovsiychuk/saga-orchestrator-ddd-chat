package com.rest_service.dto

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageProjectionDTO(
    val messageId: UUID
)
