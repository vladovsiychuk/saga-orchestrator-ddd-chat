package com.rest_service.service

import com.rest_service.command.MessageCommand
import com.rest_service.domain.Member
import com.rest_service.domain.MessageEvent
import com.rest_service.domain.User
import com.rest_service.dto.MessageDTO
import com.rest_service.enums.MessageEventType
import com.rest_service.event.MessageActionEvent
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.UserRepository
import com.rest_service.resultReader.MessageResultReader
import com.rest_service.util.MessageUtil
import com.rest_service.util.SecurityUtil
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

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

                        messageEventRepository.findProjectionMessage(roomId)
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
            memberRepository.findByRoomId(command.roomId)
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
                UUID.randomUUID(),
                UUID.randomUUID(),
                user.primaryLanguage,
                command.content,
                command.roomId,
                user.id,
                MessageEventType.MESSAGE_NEW,
                Instant.now()
                    .toEpochMilli()
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
