package com.rest_service.service

import com.rest_service.command.MessageCommand
import com.rest_service.command.TranslationCommand
import com.rest_service.domain.MessageDomain
import com.rest_service.dto.MessageDTO
import com.rest_service.exception.UnauthorizedException
import com.rest_service.manager.MessageManager
import com.rest_service.manager.RoomManager
import com.rest_service.manager.UserManager
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.zip
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Singleton
class MessageService(
    private val messageManager: MessageManager,
    private val userManager: UserManager,
    private val roomManager: RoomManager,
) {


    fun list(roomLimit: Int): Flux<MessageDTO> {
        return messageManager.collectLastMessagesInEachRoom(roomLimit)
            .map(MessageDomain::toDto)
    }

    fun getRoomMessages(roomId: UUID): Flux<MessageDTO> {
        return zip(
            userManager.getCurrentUser(),
            roomManager.findById(roomId, withMessages = true)
        )
            .flatMapMany { (currentUser, room) ->
                roomManager.validateUserIsRoomMember(currentUser, room)
                    .thenMany(room.messages.toFlux())
                    .flatMap { messageManager.findMessage(it, currentUser) }
                    .map(MessageDomain::toDto)
            }
    }

    fun create(command: MessageCommand): Mono<MessageDTO> {
        return zip(
            userManager.getCurrentUser(),
            roomManager.findById(command.roomId!!)
        )
            .flatMap { (currentUser, room) ->
                roomManager.validateUserIsRoomMember(currentUser, room)
                    .flatMap { messageManager.createMessage(command, currentUser) }
                    .flatMap { messageRR ->
                        roomManager.broadcastMessageToRoomMembers(room, messageRR)
                            .thenReturn(messageRR.toDomain(currentUser).toDto())
                    }
            }
    }

    fun update(command: MessageCommand, messageId: UUID): Mono<MessageDTO> {
        return userManager.getCurrentUser()
            .flatMap { currentUser ->
                messageManager.findMessage(messageId, currentUser)
                    .flatMap { message ->
                        if (!message.isSender(currentUser))
                            return@flatMap UnauthorizedException().toMono()

                        zip(
                            roomManager.findById(message.toDto().roomId),
                            messageManager.modifyMessageContent(message, command, currentUser)
                        ).flatMap { (room, messageRR) ->
                            roomManager.broadcastMessageToRoomMembers(room, messageRR)
                                .thenReturn(messageRR.toDomain(currentUser).toDto())
                        }
                    }
            }
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
                                        roomManager.broadcastMessageToRoomMembers(room, messageRR)
                                            .thenReturn(messageRR.toDomain(currentUser).toDto())
                                    }
                            }
                    }
            }
    }


    fun translate(messageId: UUID, command: TranslationCommand): Mono<MessageDTO> {
        return userManager.getCurrentUser()
            .flatMap { currentUser ->
                if (!currentUser.canTranslateLanguage(command.language))
                    return@flatMap UnauthorizedException().toMono()

                messageManager.findMessage(messageId, currentUser)
                    .flatMap { message ->
                        roomManager.findById(message.toDto().roomId)
                            .flatMap { room ->
                                if (!room.isRoomMember(currentUser) ||
                                    message.isTranslatedByAnotherUser(currentUser, command.language)
                                )
                                    return@flatMap UnauthorizedException().toMono()

                                messageManager.updateTranslation(message, command, currentUser)
                                    .flatMap { messageRR ->
                                        roomManager.broadcastMessageToRoomMembers(room, messageRR)
                                            .thenReturn(messageRR.toDomain(currentUser).toDto())
                                    }
                            }
                    }
            }
    }
}
