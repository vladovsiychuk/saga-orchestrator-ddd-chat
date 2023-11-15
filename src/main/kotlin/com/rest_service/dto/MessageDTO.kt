package com.rest_service.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.rest_service.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageDTO(
    val id: UUID,
    val roomId: UUID,
    val senderId: UUID,
    val content: String,
    val read: List<UUID>,
    val originalLanguage: LanguageEnum,
    val translations: List<TranslationDTO>,
    @JsonProperty("isModified")
    val modified: Boolean,
    val dateCreated: Long,
)
