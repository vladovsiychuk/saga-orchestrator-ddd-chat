package com.rest_service.dto

import com.rest_service.enums.LanguageEnum
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class UserDTO(
    val id: UUID,
    val username: String?,
    val email: String,
    val avatar: String?,
    val language: LanguageEnum,
    val dateCreated: Long,
    val dateUpdated: Long,
)
