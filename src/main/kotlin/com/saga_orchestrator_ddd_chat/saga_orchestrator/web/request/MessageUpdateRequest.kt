package com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request

import io.micronaut.core.annotation.Introspected

@Introspected
data class MessageUpdateRequest(
    val content: String,
)
