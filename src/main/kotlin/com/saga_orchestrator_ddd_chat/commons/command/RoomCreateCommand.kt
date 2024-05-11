package com.saga_orchestrator_ddd_chat.commons.command

import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class RoomCreateCommand(
    val companionId: UUID
) : Command
