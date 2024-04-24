package com.rest_service.messaging.message.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.MessageTranslateCommand
import com.rest_service.commons.dto.TranslationDTO
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent

class MessageTranslateState(private val domain: MessageDomain) : MessageState {
    constructor(domain: MessageDomain, event: MessageDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)

        if (domain.operationId == event.operationId)
            if (domain.message.translations.any { it.language == command.language })
                throw RuntimeException("Message with id ${domain.message.id} is already translate to ${command.language}.")

        val newTranslation = TranslationDTO(event.responsibleUserId, command.translation, command.language, false)
        domain.message.translations.add(newTranslation)
    }

    private val mapper = jacksonObjectMapper()

    override fun apply(event: MessageDomainEvent) = run {
        MessageCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
