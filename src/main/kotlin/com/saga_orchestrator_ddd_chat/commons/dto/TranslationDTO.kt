package com.saga_orchestrator_ddd_chat.commons.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class TranslationDTO(
    val translatorId: UUID,
    val translation: String,
    val language: LanguageEnum,
    @JsonProperty("isModified")
    val modified: Boolean,
)
