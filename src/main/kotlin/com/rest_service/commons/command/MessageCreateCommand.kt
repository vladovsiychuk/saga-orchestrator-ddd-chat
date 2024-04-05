package com.rest_service.commons.command

import com.rest_service.commons.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageCreateCommand(
    val roomId: UUID,
    val content: String,
    val language: LanguageEnum,
) : Command
