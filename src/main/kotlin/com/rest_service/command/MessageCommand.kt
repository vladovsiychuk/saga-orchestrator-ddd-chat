package com.rest_service.command

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageCommand(
    val roomId: UUID?,
    val content: String,
)
