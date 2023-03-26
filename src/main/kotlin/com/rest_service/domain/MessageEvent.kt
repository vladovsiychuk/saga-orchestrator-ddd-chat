package com.rest_service.domain

import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.MessageEventType
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.util.UUID


@MappedEntity
data class MessageEvent(
    @field:Id
    val id: UUID,
    val messageId: UUID,
    val language: LanguageEnum?,
    val content: String?,
    val roomId: UUID?,
    val responsibleId: UUID,
    val type: MessageEventType,
    @DateCreated
    val dateCreated: Long,
)
