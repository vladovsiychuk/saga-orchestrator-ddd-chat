package com.rest_service.manager

import com.rest_service.command.MessageCommand
import com.rest_service.command.TranslationCommand
import com.rest_service.domain.MessageDomain
import com.rest_service.domain.UserDomain
import com.rest_service.entity.MessageEvent
import com.rest_service.enums.MessageEventType
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MessageEventRepository
import com.rest_service.resultReader.MessageResultReader
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class MessageManager(
    private val eventRepository: MessageEventRepository,
    private val userManager: UserManager,
    private val roomManager: RoomManager,
) {
    fun collectLastMessagesInEachRoom(messageLimit: Int): Flux<MessageDomain> {
        return userManager.getCurrentUser()
            .flatMapMany { currentUser ->
                roomManager.listByUser(currentUser)
                    .map { it.takeLastMessageIds(messageLimit) }
                    .flatMapIterable { it }
                    .flatMap { this.rehydrateMessage(it) }
                    .map { it.toDomain(currentUser) }
            }
    }

    fun findMessage(messageId: UUID, user: UserDomain): Mono<MessageDomain> {
        return this.rehydrateMessage(messageId)
            .map { it.toDomain(user) }
            .switchIfEmpty(NotFoundException("Message with id $messageId doesn't exist.").toMono())
    }

    fun createMessage(command: MessageCommand, currentUserDomain: UserDomain): Mono<MessageResultReader> {
        val currentUser = currentUserDomain.toDto()

        val event = MessageEvent(
            messageId = UUID.randomUUID(),
            language = currentUser.primaryLanguage,
            content = command.content,
            roomId = command.roomId,
            responsibleId = currentUser.id,
            type = MessageEventType.MESSAGE_NEW,
        )

        return eventRepository.save(event)
            .flatMap { this.rehydrateMessage(it.messageId) }
    }

    fun modifyMessageContent(message: MessageDomain, command: MessageCommand, responsible: UserDomain): Mono<MessageResultReader> {
        val event = MessageEvent(
            messageId = message.toDto().id,
            content = command.content,
            responsibleId = responsible.toDto().id,
            type = MessageEventType.MESSAGE_MODIFY
        )

        return eventRepository.save(event)
            .flatMap { this.rehydrateMessage(it.messageId) }
    }

    fun readMessage(message: MessageDomain, responsible: UserDomain): Mono<MessageResultReader> {
        val event = MessageEvent(
            messageId = message.toDto().id,
            responsibleId = responsible.toDto().id,
            type = MessageEventType.MESSAGE_READ
        )

        return eventRepository.save(event)
            .flatMap { this.rehydrateMessage(it.messageId) }
    }

    fun updateTranslation(message: MessageDomain, command: TranslationCommand, currentUser: UserDomain): Mono<MessageResultReader> {
        val type = if (message.translationExists(command.language))
            MessageEventType.MESSAGE_TRANSLATE_MODIFY
        else
            MessageEventType.MESSAGE_TRANSLATE

        val event = MessageEvent(
            messageId = message.toDto().id,
            language = command.language,
            content = command.translation,
            responsibleId = currentUser.toDto().id,
            type = type,
        )

        return eventRepository.save(event)
            .flatMap { this.rehydrateMessage(it.messageId) }
    }

    private fun rehydrateMessage(messageId: UUID): Mono<MessageResultReader> {
        return eventRepository.findByMessageIdOrderByDateCreated(messageId)
            .reduce(MessageResultReader(messageId)) { resultReader, event ->
                resultReader.apply(event)
                resultReader
            }
    }
}
