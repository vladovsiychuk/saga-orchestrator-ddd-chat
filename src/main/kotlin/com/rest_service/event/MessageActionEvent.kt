package com.rest_service.event

import com.rest_service.resultReader.MessageResultReader
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageActionEvent(
    val userId: UUID,
    val message: MessageResultReader
)
