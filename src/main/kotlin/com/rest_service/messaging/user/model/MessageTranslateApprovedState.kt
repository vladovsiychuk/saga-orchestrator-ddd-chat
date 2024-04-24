package com.rest_service.messaging.user.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.command.MessageTranslateCommand
import com.rest_service.commons.enums.UserType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent

class MessageTranslateApprovedState(private val domain: UserDomain) : UserState {
    constructor(domain: UserDomain, event: UserDomainEvent) : this(domain) {
        val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)
        val user = domain.currentUser

        if (domain.operationId == event.operationId)
            when {
                user.type != UserType.TRANSLATOR                        ->
                    throw RuntimeException("User with id ${user.id} is not a translator.")

                !user.translationLanguages!!.contains(command.language) ->
                    throw RuntimeException("User with id ${user.id} cannot translate ${command.language}")
            }
    }

    private val mapper = jacksonObjectMapper()

    override fun apply(event: UserDomainEvent) = run {
        UserCreatedState(domain)
            .let {
                domain.changeState(it)
                it.apply(event)
            }
    }
}
