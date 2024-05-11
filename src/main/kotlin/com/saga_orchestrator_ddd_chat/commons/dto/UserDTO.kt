package com.saga_orchestrator_ddd_chat.commons.dto

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import com.saga_orchestrator_ddd_chat.commons.enums.UserType
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class UserDTO(
    val id: UUID,
    val username: String?,
    val email: String,
    val avatar: String?,
    val primaryLanguage: LanguageEnum,
    val translationLanguages: Set<LanguageEnum>?,
    val type: UserType,
    val dateCreated: Long,
    val dateUpdated: Long,
) : DTO
