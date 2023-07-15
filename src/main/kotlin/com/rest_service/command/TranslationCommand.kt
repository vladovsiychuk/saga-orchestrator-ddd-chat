package com.rest_service.command

import com.rest_service.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected

@Introspected
data class TranslationCommand(
    val translation: String,
    val language: LanguageEnum,
)
