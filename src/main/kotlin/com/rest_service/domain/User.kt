package com.rest_service.domain

import com.rest_service.command.UserCommand
import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import com.rest_service.util.SecurityUtil
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.time.Instant
import java.util.UUID


@MappedEntity
data class User(
    @field:Id
    val id: UUID,
    val username: String?,
    val email: String,
    val avatar: String?,
    val primaryLanguage: LanguageEnum,
    @MappedProperty(type = DataType.JSON)
    val translationLanguages: List<String>?,
    val type: UserType,
    val dateCreated: Long = Instant.now()
        .toEpochMilli(),
    val dateUpdated: Long = Instant.now()
        .toEpochMilli(),
) {
    constructor(command: UserCommand, securityUtil: SecurityUtil) : this(
        UUID.randomUUID(),
        command.username,
        securityUtil.getUserEmail(),
        null,
        command.primaryLanguage,
        command.translationLanguages?.map { it.toString() },
        command.type,
    )
}
