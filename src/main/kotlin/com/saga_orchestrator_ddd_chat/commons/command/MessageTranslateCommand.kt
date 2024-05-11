package com.saga_orchestrator_ddd_chat.commons.command

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageTranslateCommand(
    val messageId: UUID,
    val translation: String,
    val language: LanguageEnum,
) : Command
