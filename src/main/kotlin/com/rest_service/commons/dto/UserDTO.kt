package com.rest_service.commons.dto

import com.rest_service.commons.command.UserCommand
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
    val translationLanguages: List<LanguageEnum>?,
    val type: UserType,
    val dateCreated: Long,
    val dateUpdated: Long,
) : DTO {
    constructor(command: UserCommand, dateCreated: Long) : this(
        command.id!!,
        command.temporaryId,
        command.username,
        command.email,
        null,
        command.primaryLanguage,
        command.translationLanguages?.toList(),
        command.type,
        dateCreated,
        dateCreated,
    )
}
