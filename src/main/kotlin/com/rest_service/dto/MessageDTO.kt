package com.rest_service.dto

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
    val translation: String,
    val dateCreated: Long,
)
