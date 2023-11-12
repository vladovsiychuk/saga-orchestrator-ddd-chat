package com.rest_service.service

import com.rest_service.command.MessageCommand
import com.rest_service.command.TranslationCommand
import com.rest_service.dto.MessageDTO
import com.rest_service.entity.Member
import com.rest_service.entity.MessageEvent
import com.rest_service.entity.User
import com.rest_service.enums.MessageEventType
import com.rest_service.enums.UserType
import com.rest_service.event.MessageActionEvent
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.exception.UnauthorizedException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.UserRepository
import com.rest_service.resultReader.MessageResultReader
import com.rest_service.util.MessageUtil
import com.rest_service.util.SecurityUtil
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class MessageService(
    private val securityUtil: SecurityUtil,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val messageEventRepository: MessageEventRepository,
    private val messageUtil: MessageUtil,
    private val applicationEventPublisher: ApplicationEventPublisher<MessageActionEvent>
) {


    fun list(roomLimit: Int): Flux<MessageDTO> {
        return messageUtil.findLastMessagesPerRoom(roomLimit)
    }

    fun getRoomMessages(roomId: UUID): Flux<MessageDTO> {
        val email = securityUtil.getUserEmail()

        return Mono.zip(
            userRepository.findByEmail(email),
            memberRepository.findByRoomId(roomId)
                .switchIfEmpty(Flux.error(NotFoundException("Room with id $roomId doesn't exist.")))
                .collectList()
        )
            .flux()
            .flatMap { result ->
                val user = result.t1
                val roomMembers = result.t2

                validateUserIsRoomMember(user, roomMembers, roomId)
                    .flux()
                    .flatMap {

                        messageEventRepository.findProjectionByRoomId(roomId)
                            .flatMap {

                                messageUtil.rehydrateMessage(it.messageId)
                                    .map { messageResultReader ->
                                        messageResultReader.toDto(user)
                                    }
                            }
                    }
            }
    }

    fun create(command: MessageCommand): Mono<MessageDTO> {
        val userEmail = securityUtil.getUserEmail()

        return Mono.zip(
            userRepository.findByEmail(userEmail),
            memberRepository.findByRoomId(command.roomId!!)
                .switchIfEmpty(Flux.error(NotFoundException("Room with id ${command.roomId} doesn't exist.")))
                .collectList()
        )
            .flatMap { result ->
                val user = result.t1
                val roomMembers = result.t2

                validateUserIsRoomMember(user, roomMembers, command.roomId)
                    .flatMap {

                        createMessageEvent(user, command)
                            .flatMap { messageEvent ->

                                saveMessageEvent(messageEvent)
                                    .flatMap { savedMessageEvent ->

                                        messageUtil.rehydrateMessage(savedMessageEvent.messageId)
                                            .map { messageResultReader ->
                                                val roomMemberIds = roomMembers.map { it.userId }
                                                broadcastMessageToRoomMembers(messageResultReader, roomMemberIds)

                                                messageResultReader.toDto(user)
                                            }
                                    }
                            }
                    }
            }
    }

    fun update(command: MessageCommand, messageId: UUID): Mono<MessageDTO> {
        val userEmail = securityUtil.getUserEmail()

        return Mono.zip(
            userRepository.findByEmail(userEmail),
            messageUtil.rehydrateMessage(messageId)
        ).flatMap { result ->
            val user = result.t1
            val messageResultReader = result.t2

            val messageDTO = messageResultReader.toDto(user)

            if (messageDTO.senderId != user.id)
                return@flatMap Mono.error(UnauthorizedException())

            val event = MessageEvent(
                messageId = messageId,
                content = command.content,
                responsibleId = user.id,
                type = MessageEventType.MESSAGE_MODIFY
            )

            Mono.zip(
                saveMessageEvent(event),
                memberRepository.findByRoomId(messageDTO.roomId)
                    .collectList()
            ).map {
                val savedEvent = it.t1
                val roomMembers = it.t2.map { member -> member.userId }

                messageResultReader.apply(savedEvent)
                broadcastMessageToRoomMembers(messageResultReader, roomMembers)

                messageResultReader.toDto(user)
            }
        }
    }

    fun read(id: UUID): Mono<MessageDTO> {
        val email = securityUtil.getUserEmail()

        return Mono.zip(
            userRepository.findByEmail(email),
            messageUtil.rehydrateMessage(id)
        ).flatMap { result ->
            val user = result.t1
            val messageRR = result.t2
            val roomId = messageRR.message.roomId

            memberRepository.findByRoomId(roomId!!)
                .collectList()
                .flatMap { members ->
                    validateUserIsRoomMember(user, members, roomId)
                        .flatMap {
                            val messageEvent = MessageEvent(
                                messageId = id,
                                responsibleId = user.id!!,
                                type = MessageEventType.MESSAGE_READ
                            )

                            messageEventRepository.save(messageEvent)
                                .map { event ->
                                    messageRR.apply(event)
                                    broadcastMessageToRoomMembers(messageRR, members.map { it.userId })

                                    messageRR.toDto(user)
                                }
                        }
                }
        }
    }


    fun translate(messageId: UUID, command: TranslationCommand): Mono<MessageDTO> {
        val email = securityUtil.getUserEmail()

        return userRepository.findByEmail(email)
            .flatMap { currentUser ->

                if (currentUser.type != UserType.TRANSLATOR || command.language.toString() !in currentUser.translationLanguages!!)
                    return@flatMap Mono.error(UnauthorizedException())

                messageUtil.rehydrateMessage(messageId)
                    .flatMap { messageResultReader ->

                        val message = messageResultReader.toDto(currentUser)

                        memberRepository.findByRoomId(message.roomId)
                            .collectList()
                            .flatMap { roomMembers ->

                                val existingTranslation = message.translations.find { it.language == command.language }

                                if (
                                    currentUser.id !in roomMembers.map { it.userId }
                                    || existingTranslation != null && existingTranslation.translatorId != currentUser.id
                                )
                                    return@flatMap Mono.error(UnauthorizedException())

                                val type = if (existingTranslation != null)
                                    MessageEventType.MESSAGE_TRANSLATE_MODIFY
                                else
                                    MessageEventType.MESSAGE_TRANSLATE

                                val event = MessageEvent(
                                    messageId = messageId,
                                    language = command.language,
                                    content = command.translation,
                                    responsibleId = currentUser.id!!,
                                    type = type,
                                )

                                messageEventRepository.save(event)
                                    .map {
                                        messageResultReader.apply(event)
                                        messageResultReader.toDto(currentUser)
                                    }
                            }
                    }
            }
    }


    private fun validateUserIsRoomMember(user: User, roomMembers: List<Member>, roomId: UUID): Mono<Boolean> {
        val roomMemberIds = roomMembers.map { it.userId }
        if (user.id !in roomMemberIds) {
            return Mono.error(IncorrectInputException("User with id ${user.id} is not a member of room with id $roomId"))
        }

        return Mono.just(true)
    }

    private fun createMessageEvent(user: User, command: MessageCommand): Mono<MessageEvent> {
        return Mono.just(
            MessageEvent(
                messageId = UUID.randomUUID(),
                language = user.primaryLanguage,
                content = command.content,
                roomId = command.roomId,
                responsibleId = user.id!!,
                type = MessageEventType.MESSAGE_NEW,
            )
        )
    }

    private fun saveMessageEvent(messageEvent: MessageEvent): Mono<MessageEvent> {
        return messageEventRepository.save(messageEvent)
    }

    private fun broadcastMessageToRoomMembers(
        messageResultReader: MessageResultReader,
        roomMemberIds: List<UUID>
    ) {
        roomMemberIds.forEach { memberId ->
            val event = MessageActionEvent(memberId, messageResultReader)
            applicationEventPublisher.publishEventAsync(event)
        }
    }
}
