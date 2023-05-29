package com.rest_service.domain

import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.MessageEventType
import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import java.time.Instant
import java.util.UUID


@MappedEntity
data class MessageEvent(
    @field:Id
    @AutoPopulated
    val id: UUID? = null,
    val messageId: UUID,
    val language: LanguageEnum? = null,
    val content: String? = null,
    val roomId: UUID? = null,
    val responsibleId: UUID,
    val type: MessageEventType,
    val dateCreated: Long = Instant.now()
        .toEpochMilli(),
)
