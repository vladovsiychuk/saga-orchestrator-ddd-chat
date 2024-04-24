package com.rest_service.commons.dto

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
    val read: MutableList<UUID>,
    val originalLanguage: LanguageEnum,
    val translations: MutableList<TranslationDTO>,
    val modified: Boolean,
    val dateCreated: Long,
) : DTO {
    constructor(command: MessageCreateCommand, event: MessageDomainEvent) : this(
        event.messageId,
        command.roomId,
        event.responsibleUserId,
        command.content,
        mutableListOf(),
        command.language,
        mutableListOf(),
        false,
        event.dateCreated,
    )
}
