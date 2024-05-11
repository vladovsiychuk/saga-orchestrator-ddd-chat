package com.saga_orchestrator_ddd_chat.read_service.entity

import com.saga_orchestrator_ddd_chat.commons.dto.TranslationDTO
import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.annotation.MappedProperty
import io.micronaut.data.model.DataType
import java.util.UUID

@MappedEntity
data class MessageView(
    @field:Id
    val id: UUID,
    val roomId: UUID,
    val senderId: UUID,
    var content: String,
    @MappedProperty(type = DataType.JSON)
    val read: MutableList<UUID>,
    val originalLanguage: LanguageEnum,
    @MappedProperty(type = DataType.JSON)
    val translations: MutableList<TranslationDTO>,
    val modified: Boolean,
    val dateCreated: Long,
)
