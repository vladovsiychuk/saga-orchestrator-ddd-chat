package com.rest_service.event

import com.rest_service.dto.MessageDTO
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageActionEvent(
    val userId: UUID,
    val message: MessageDTO,
) : ActionEvent
