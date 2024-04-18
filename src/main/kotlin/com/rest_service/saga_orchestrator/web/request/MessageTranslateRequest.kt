package com.rest_service.saga_orchestrator.web.request

import com.rest_service.commons.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected

@Introspected
data class MessageTranslateRequest(
    val translation: String,
    val language: LanguageEnum,
)
