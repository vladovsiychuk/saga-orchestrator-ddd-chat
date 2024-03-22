package com.rest_service.commons.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.rest_service.commons.enums.LanguageEnum
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
