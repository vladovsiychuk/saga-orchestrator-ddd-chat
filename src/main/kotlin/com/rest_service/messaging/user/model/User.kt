package com.rest_service.messaging.user.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.MessageTranslateCommand
import com.rest_service.commons.command.UserCreateCommand
import com.rest_service.commons.dto.DTO
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.UserType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventType
import java.util.UUID

class User : Domain {
    private var status = UserStatus.IN_CREATION
    private lateinit var user: UserData

    private val mapper = jacksonObjectMapper()

    fun apply(event: UserDomainEvent): DomainEvent {
        when (event.type) {
            UserDomainEventType.USER_CREATED               -> handleUserCreated(event)
            UserDomainEventType.MESSAGE_TRANSLATE_APPROVED -> approveMessageTranslate(event)

            UserDomainEventType.ROOM_CREATE_APPROVED,
            UserDomainEventType.ROOM_ADD_MEMBER_APPROVED,
            UserDomainEventType.MESSAGE_UPDATE_APPROVED,
            UserDomainEventType.MESSAGE_READ_APPROVED      -> checkForUserCreatedStatus()

            else                                           -> {}
        }

        return event
    }

    private fun handleUserCreated(event: UserDomainEvent) {
        checkForUserInCreationStatus()
        val command = mapper.convertValue(event.payload, UserCreateCommand::class.java)

        if (UUID.nameUUIDFromBytes(command.email.toByteArray()) != event.responsibleUserId)
            throw RuntimeException("Responsible user doesn't have permissions to create the user")

        user = UserData(
            event.userId,
            command.username,
            command.email,
            null,
            command.primaryLanguage,
            if (command.type == UserType.TRANSLATOR) TranslationLanguages.from(command) else null,
            command.type,
            event.dateCreated,
            event.dateCreated,
        )

        status = UserStatus.CREATED
    }

    private fun approveMessageTranslate(event: UserDomainEvent) {
        checkForUserCreatedStatus()

        val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)

        when {
            user.type != UserType.TRANSLATOR                        ->
                throw RuntimeException("User with id ${user.id} is not a translator.")

            !user.translationLanguages!!.contains(command.language) ->
                throw RuntimeException("User with id ${user.id} cannot translate ${command.language}")
        }
    }

    private fun checkForUserCreatedStatus() {
        if (status != UserStatus.CREATED)
            throw RuntimeException("User is not yet created.")
    }

    private fun checkForUserInCreationStatus() {
        if (status != UserStatus.IN_CREATION)
            throw RuntimeException("User is already created.")
    }

    override fun toDto(): DTO {
        return UserDTO(
            user.id,
            user.username,
            user.email,
            user.avatar,
            user.primaryLanguage,
            user.translationLanguages?.languages,
            user.type,
            user.dateCreated,
            user.dateUpdated,
        )
    }

    private data class UserData(
        val id: UUID,
        val username: String?,
        val email: String,
        val avatar: String?,
        val primaryLanguage: LanguageEnum,
        val translationLanguages: TranslationLanguages?,
        val type: UserType,
        val dateCreated: Long,
        val dateUpdated: Long,
    )

    private enum class UserStatus {
        IN_CREATION,
        CREATED
    }
}
