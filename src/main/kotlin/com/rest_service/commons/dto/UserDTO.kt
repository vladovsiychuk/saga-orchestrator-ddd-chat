package com.rest_service.commons.dto

import com.rest_service.commons.command.UserCreateCommand
import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class UserDTO(
    val id: UUID,
    val temporaryId: UUID?,
    val username: String?,
    val email: String,
    val avatar: String?,
    val primaryLanguage: LanguageEnum,
    val translationLanguages: Set<LanguageEnum>?,
    val type: UserType,
    val dateCreated: Long,
    val dateUpdated: Long,
) : DTO {
    constructor(command: UserCreateCommand, userId: UUID, dateCreated: Long) : this(
        userId,
        command.temporaryId,
        command.username,
        command.email,
        null,
        command.primaryLanguage,
        command.translationLanguages,
        command.type,
        dateCreated,
        dateCreated,
    )
}
