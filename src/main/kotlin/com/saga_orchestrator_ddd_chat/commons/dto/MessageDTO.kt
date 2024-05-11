package com.saga_orchestrator_ddd_chat.commons.dto

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageDTO(
    val id: UUID,
    val roomId: UUID,
    val senderId: UUID,
    val content: String,
    val read: MutableList<UUID>,
    val originalLanguage: LanguageEnum,
    val translations: MutableList<TranslationDTO>,
    val modified: Boolean,
    val dateCreated: Long,
) : DTO
