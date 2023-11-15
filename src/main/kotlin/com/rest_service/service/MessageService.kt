package com.rest_service.service

import com.rest_service.command.MessageCommand
import com.rest_service.command.TranslationCommand
import com.rest_service.dto.MessageDTO
import com.rest_service.exception.UnauthorizedException
import com.rest_service.util.MessageUtil
import com.rest_service.util.RoomUtil
import com.rest_service.util.UserUtil
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class MessageService(
    private val messageUtil: MessageUtil,
    private val userUtil: UserUtil,
    private val roomUtil: RoomUtil,
) {


    fun list(roomLimit: Int): Flux<MessageDTO> {
        return messageUtil.collectLastMessagesInEachRoom(roomLimit)
            .map { message -> message.toDto() }
    }

    fun getRoomMessages(roomId: UUID): Flux<MessageDTO> {
        return Mono.zip(
            userUtil.getCurrentUser(),
            roomUtil.findById(roomId, withMessages = true)
        )
            .flux()
            .flatMap { result ->
                val currentUser = result.t1
                val room = result.t2

                roomUtil.validateUserIsRoomMember(currentUser, room)
                    .flux()
                    .flatMap { Flux.fromIterable(room.messages) }
                    .flatMap { messageUtil.findMessage(it, currentUser) }
            }.map { it.toDto() }
    }

    fun create(command: MessageCommand): Mono<MessageDTO> {
        return Mono.zip(
            userUtil.getCurrentUser(),
            roomUtil.findById(command.roomId!!)
        ) { currentUser, room ->
            roomUtil.validateUserIsRoomMember(currentUser, room)
                .flatMap { messageUtil.createMessage(command, currentUser) }
                .flatMap { messageRR ->
                    Mono.zip(
                        roomUtil.broadcastMessageToRoomMembers(room, messageRR),
                        Mono.just(messageRR.toDomain(currentUser).toDto())
                    ) { _, message -> message }
                }
        }.flatMap { it }
    }

    fun update(command: MessageCommand, messageId: UUID): Mono<MessageDTO> {
        return userUtil.getCurrentUser()
            .flatMap { currentUser ->
                messageUtil.findMessage(messageId, currentUser)
                    .flatMap { message ->
                        if (!message.isSender(currentUser))
                            return@flatMap Mono.error(UnauthorizedException())

                        Mono.zip(
                            roomUtil.findById(message.toDto().roomId),
                            messageUtil.modifyMessageContent(message, command, currentUser)
                        ) { room, messageRR ->

                            Mono.zip(
                                roomUtil.broadcastMessageToRoomMembers(room, messageRR),
                                Mono.just(messageRR.toDomain(currentUser).toDto())
                            ) { _, message -> message }
                        }
                    }
            }.flatMap { it }
    }

    fun read(messageId: UUID): Mono<MessageDTO> {
        return userUtil.getCurrentUser()
            .flatMap { currentUser ->
                messageUtil.findMessage(messageId, currentUser)
                    .flatMap { message ->
                        roomUtil.findById(message.toDto().roomId)
                            .flatMap { room ->
                                roomUtil.validateUserIsRoomMember(currentUser, room)
                                    .flatMap { messageUtil.readMessage(message, currentUser) }
                                    .flatMap { messageRR ->
                                        Mono.zip(
                                            roomUtil.broadcastMessageToRoomMembers(room, messageRR),
                                            Mono.just(messageRR.toDomain(currentUser).toDto())
                                        ) { _, message -> message }
                                    }
                            }
                    }
            }
    }


    fun translate(messageId: UUID, command: TranslationCommand): Mono<MessageDTO> {
        return userUtil.getCurrentUser()
            .flatMap { currentUser ->
                if (!currentUser.canTranslateLanguage(command.language))
                    return@flatMap Mono.error(UnauthorizedException())

                messageUtil.findMessage(messageId, currentUser)
                    .flatMap { message ->
                        roomUtil.findById(message.toDto().roomId)
                            .flatMap { room ->
                                if (!room.isRoomMember(currentUser) ||
                                    message.isTranslatedByAnotherUser(currentUser, command.language)
                                )
                                    return@flatMap Mono.error(UnauthorizedException())

                                messageUtil.updateTranslation(message, command, currentUser)
                                    .flatMap { messageRR ->
                                        Mono.zip(
                                            roomUtil.broadcastMessageToRoomMembers(room, messageRR),
                                            Mono.just(messageRR.toDomain(currentUser).toDto())
                                        ) { _, message -> message }
                                    }
                            }
                    }
            }
    }
}
