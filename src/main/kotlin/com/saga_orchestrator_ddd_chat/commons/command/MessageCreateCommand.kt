package com.saga_orchestrator_ddd_chat.commons.command

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageCreateCommand(
    val roomId: UUID,
    val content: String,
    val language: LanguageEnum,
) : Command
