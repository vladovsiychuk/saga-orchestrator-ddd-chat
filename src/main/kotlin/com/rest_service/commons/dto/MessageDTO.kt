package com.rest_service.commons.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.rest_service.commons.command.MessageCreateCommand
import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import io.micronaut.core.annotation.Introspected
import java.util.UUID

@Introspected
data class MessageDTO(
    val id: UUID,
    val roomId: UUID,
    val senderId: UUID,
    var content: String,
    val read: List<UUID>,
    val originalLanguage: LanguageEnum,
    val translations: List<TranslationDTO>,
    @JsonProperty("isModified")
    val modified: Boolean,
    val dateCreated: Long,
) : DTO {
    constructor(command: MessageCreateCommand, event: MessageDomainEvent) : this(
        event.messageId,
        command.roomId,
        event.responsibleUserId,
        command.content,
        listOf(),
        command.language,
        listOf(),
        false,
        event.dateCreated,
    )
}
