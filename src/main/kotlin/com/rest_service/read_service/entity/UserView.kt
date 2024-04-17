package com.rest_service.read_service.entity

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.util.UUID

@MappedEntity
data class UserView(
    @field:Id
    val id: UUID,
    val username: String?,
    val email: String,
    val avatar: String?,
    val primaryLanguage: LanguageEnum,
    @MappedProperty(type = DataType.JSON)
    val translationLanguages: List<LanguageEnum>?,
    val type: UserType,
    val dateCreated: Long,
    val dateUpdated: Long,
)
