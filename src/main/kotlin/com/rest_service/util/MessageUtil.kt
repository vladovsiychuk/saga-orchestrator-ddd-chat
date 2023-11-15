package com.rest_service.util

import com.rest_service.command.MessageCommand
import com.rest_service.command.TranslationCommand
import com.rest_service.domain.MessageDomain
import com.rest_service.domain.UserDomain
import com.rest_service.entity.MessageEvent
import com.rest_service.enums.MessageEventType
import com.rest_service.repository.MessageEventRepository
import com.rest_service.resultReader.MessageResultReader
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class MessageUtil(
    private val eventRepository: MessageEventRepository,
    private val userUtil: UserUtil,
    private val roomUtil: RoomUtil,
) {

    /**
     * Collects the last n messages in each room for the current user.
     *
     * 1. get the current user
     * 2. get user rooms
     * 3. take last n messages per room
     * 4. rehydrate each message
     * 5. @return list of MessageDomain
     */
    fun collectLastMessagesInEachRoom(messageLimit: Int): Flux<MessageDomain> {
        return userUtil.getCurrentUser()
            .flux()
            .flatMap { currentUser ->
                roomUtil.listByUser(currentUser)
                    .map { it.takeLastMessageIds(messageLimit) }
                    .flatMap { Flux.fromIterable(it) }
                    .flatMap { this.rehydrateMessage(it) }
                    .map { it.toDomain(currentUser) }
            }
    }

    fun findMessage(messageId: UUID, user: UserDomain): Mono<MessageDomain> {
        return rehydrateMessage(messageId)
            .map { it.toDomain(user) }
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
            .collectList()
            .map { events ->
                val resultReader = MessageResultReader(messageId)

                events.forEach {
                    resultReader.apply(it)
                }

                resultReader
            }
    }
}
