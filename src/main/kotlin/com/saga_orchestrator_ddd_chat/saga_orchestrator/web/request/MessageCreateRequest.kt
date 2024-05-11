package com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageCreateRequest(
    val roomId: UUID,
    val content: String,
)
