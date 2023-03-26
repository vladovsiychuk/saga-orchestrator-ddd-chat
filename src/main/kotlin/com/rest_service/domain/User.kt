package com.rest_service.domain

import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
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
    val dateCreated: Long,
    val dateUpdated: Long,
)
