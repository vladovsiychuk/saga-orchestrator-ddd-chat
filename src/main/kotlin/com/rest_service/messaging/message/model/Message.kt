package com.rest_service.messaging.message.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.MessageCreateCommand
import com.rest_service.commons.command.MessageTranslateCommand
import com.rest_service.commons.command.MessageUpdateCommand
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.commons.dto.TranslationDTO
import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType
import java.util.UUID

class Message : Domain {
    private var status = MessageStatus.IN_CREATION
    private lateinit var message: MessageData

    private val mapper = jacksonObjectMapper()

    fun apply(event: MessageDomainEvent): DomainEvent {
        when (event.type) {
            MessageDomainEventType.MESSAGE_CREATED    -> handleMessageCreated(event)
            MessageDomainEventType.MESSAGE_UPDATED    -> handleMessageUpdated(event)
            MessageDomainEventType.MESSAGE_READ       -> handleMessageRead(event)
            MessageDomainEventType.MESSAGE_TRANSLATED -> handleMessageTranslated(event)
            else                                      -> {}
        }

        return event
    }

    private fun handleMessageCreated(event: MessageDomainEvent) {
        checkForMessageInCreationStatus()
        val command = mapper.convertValue(event.payload, MessageCreateCommand::class.java)

        message = MessageData(
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

        status = MessageStatus.CREATED
    }

    private fun handleMessageUpdated(event: MessageDomainEvent) {
        checkForMessageCreatedStatus()
        val command = mapper.convertValue(event.payload, MessageUpdateCommand::class.java)
        message.content = command.content
    }

    private fun handleMessageRead(event: MessageDomainEvent) {
        checkForMessageCreatedStatus()
        message.read.add(event.responsibleUserId)
    }

    private fun handleMessageTranslated(event: MessageDomainEvent) {
        checkForMessageCreatedStatus()
        val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)

        if (message.translations.any { it.language == command.language })
            throw RuntimeException("Message with id ${message.id} is already translate to ${command.language}.")

        val newTranslation = TranslationDTO(event.responsibleUserId, command.translation, command.language, false)
        message.translations.add(newTranslation)
    }

    private fun checkForMessageCreatedStatus() {
        if (status != MessageStatus.CREATED)
            throw RuntimeException("Message is not yet created.")
    }

    private fun checkForMessageInCreationStatus() {
        if (status != MessageStatus.IN_CREATION)
            throw RuntimeException("Message is already created.")
    }

    override fun toDto(): MessageDTO {
        return MessageDTO(
            message.id,
            message.roomId,
            message.senderId,
            message.content,
            message.read,
            message.originalLanguage,
            message.translations,
            message.modified,
            message.dateCreated,
        )
    }

    private data class MessageData(
        val id: UUID,
        val roomId: UUID,
        val senderId: UUID,
        var content: String,
        val read: MutableList<UUID>,
        val originalLanguage: LanguageEnum,
        val translations: MutableList<TranslationDTO>,
        val modified: Boolean,
        val dateCreated: Long,
    )

    private enum class MessageStatus {
        IN_CREATION,
        CREATED,
    }
}
