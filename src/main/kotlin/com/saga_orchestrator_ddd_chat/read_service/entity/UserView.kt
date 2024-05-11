package com.saga_orchestrator_ddd_chat.read_service.entity

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import com.saga_orchestrator_ddd_chat.commons.enums.UserType
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
