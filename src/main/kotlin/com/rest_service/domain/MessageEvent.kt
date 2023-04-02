package com.rest_service.domain

import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.MessageEventType
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.Instant
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
    val dateCreated: Long = Instant.now()
        .toEpochMilli(),
)
