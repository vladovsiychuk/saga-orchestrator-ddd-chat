package com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected

@Introspected
data class MessageTranslateRequest(
    val translation: String,
    val language: LanguageEnum,
)
