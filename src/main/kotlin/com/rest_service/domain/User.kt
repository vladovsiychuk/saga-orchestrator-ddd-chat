package com.rest_service.domain

import com.rest_service.enums.LanguageEnum
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID


@MappedEntity
data class User(
    @field:Id
    val id: UUID,
    val username: String?,
    val email: String,
    val avatar: String?,
    val language: LanguageEnum,
    val dateCreated: Long,
    val dateUpdated: Long,
)
