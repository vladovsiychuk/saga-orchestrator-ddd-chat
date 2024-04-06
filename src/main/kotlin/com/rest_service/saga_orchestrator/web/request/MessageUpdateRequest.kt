package com.rest_service.saga_orchestrator.web.request

import io.micronaut.core.annotation.Introspected

@Introspected
data class MessageUpdateRequest(
    val content: String,
)
