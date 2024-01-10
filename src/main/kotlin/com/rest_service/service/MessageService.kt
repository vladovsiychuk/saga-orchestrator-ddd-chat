package com.rest_service.service

import com.rest_service.command.MessageCommand
import com.rest_service.command.TranslationCommand
import com.rest_service.dto.MessageDTO
import com.rest_service.exception.UnauthorizedException
import com.rest_service.manager.MessageManager
import com.rest_service.manager.RoomManager
import com.rest_service.manager.UserManager
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class MessageService(
    private val messageManager: MessageManager,
    private val userManager: UserManager,
    private val roomManager: RoomManager,
) {


    fun list(roomLimit: Int): Flux<MessageDTO> {
        return messageManager.collectLastMessagesInEachRoom(roomLimit)
            .map { message -> message.toDto() }
    }

    fun getRoomMessages(roomId: UUID): Flux<MessageDTO> {
        return Mono.zip(
            userManager.getCurrentUser(),
            roomManager.findById(roomId, withMessages = true)
        )
            .flux()
            .flatMap { result ->
                val currentUser = result.t1
                val room = result.t2

                roomManager.validateUserIsRoomMember(currentUser, room)
                    .flux()
                    .flatMap { Flux.fromIterable(room.messages) }
                    .flatMap { messageManager.findMessage(it, currentUser) }
            }.map { it.toDto() }
    }

    fun create(command: MessageCommand): Mono<MessageDTO> {
        return Mono.zip(
            userManager.getCurrentUser(),
            roomManager.findById(command.roomId!!)
        ) { currentUser, room ->
            roomManager.validateUserIsRoomMember(currentUser, room)
                .flatMap { messageManager.createMessage(command, currentUser) }
                .flatMap { messageRR ->
                    Mono.zip(
                        roomManager.broadcastMessageToRoomMembers(room, messageRR),
                        Mono.just(messageRR.toDomain(currentUser).toDto())
                    ) { _, message -> message }
                }
        }.flatMap { it }
    }

    fun update(command: MessageCommand, messageId: UUID): Mono<MessageDTO> {
        return userManager.getCurrentUser()
            .flatMap { currentUser ->
                messageManager.findMessage(messageId, currentUser)
                    .flatMap { message ->
                        if (!message.isSender(currentUser))
                            return@flatMap Mono.error(UnauthorizedException())

                        Mono.zip(
                            roomManager.findById(message.toDto().roomId),
                            messageManager.modifyMessageContent(message, command, currentUser)
                        ) { room, messageRR ->

                            Mono.zip(
                                roomManager.broadcastMessageToRoomMembers(room, messageRR),
                                Mono.just(messageRR.toDomain(currentUser).toDto())
                            ) { _, message -> message }
                        }
                    }
            }.flatMap { it }
    }

    fun read(messageId: UUID): Mono<MessageDTO> {
        return userManager.getCurrentUser()
            .flatMap { currentUser ->
                messageManager.findMessage(messageId, currentUser)
                    .flatMap { message ->
                        roomManager.findById(message.toDto().roomId)
                            .flatMap { room ->
                                roomManager.validateUserIsRoomMember(currentUser, room)
                                    .flatMap { messageManager.readMessage(message, currentUser) }
                                    .flatMap { messageRR ->
                                        Mono.zip(
                                            roomManager.broadcastMessageToRoomMembers(room, messageRR),
                                            Mono.just(messageRR.toDomain(currentUser).toDto())
                                        ) { _, message -> message }
                                    }
                            }
                    }
            }
    }


    fun translate(messageId: UUID, command: TranslationCommand): Mono<MessageDTO> {
        return userManager.getCurrentUser()
            .flatMap { currentUser ->
                if (!currentUser.canTranslateLanguage(command.language))
                    return@flatMap Mono.error(UnauthorizedException())

                messageManager.findMessage(messageId, currentUser)
                    .flatMap { message ->
                        roomManager.findById(message.toDto().roomId)
                            .flatMap { room ->
                                if (!room.isRoomMember(currentUser) ||
                                    message.isTranslatedByAnotherUser(currentUser, command.language)
                                )
                                    return@flatMap Mono.error(UnauthorizedException())

                                messageManager.updateTranslation(message, command, currentUser)
                                    .flatMap { messageRR ->
                                        Mono.zip(
                                            roomManager.broadcastMessageToRoomMembers(room, messageRR),
                                            Mono.just(messageRR.toDomain(currentUser).toDto())
                                        ) { _, message -> message }
                                    }
                            }
                    }
            }
    }
}
