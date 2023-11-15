package com.rest_service.domain

import com.rest_service.dto.MessageDTO
import com.rest_service.dto.TranslationDTO
import com.rest_service.enums.LanguageEnum
import java.util.UUID

class MessageDomain(
    private val id: UUID,
    private val roomId: UUID,
    private val senderId: UUID,
    private val content: String,
    private val read: List<UUID>,
    private val originalLanguage: LanguageEnum,
    private val translations: List<TranslationDTO>,
    private val modified: Boolean = false,
    private val dateCreated: Long,
) {
    fun toDto(): MessageDTO {
        return MessageDTO(
            id,
            roomId,
            senderId,
            content,
            read,
            originalLanguage,
            translations,
            modified,
            dateCreated
        )
    }

    fun isSender(currentUser: UserDomain): Boolean {
        return senderId == currentUser.toDto().id
    }

    fun isTranslatedByAnotherUser(currentUser: UserDomain, language: LanguageEnum): Boolean {
        return translations.any { it.language == language && it.translatorId != currentUser.toDto().id }
    }

    fun translationExists(language: LanguageEnum): Boolean {
        return translations.any { it.language == language }
    }
}
