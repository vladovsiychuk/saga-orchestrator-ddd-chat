package com.rest_service.service

import com.rest_service.command.MessageCommand
import com.rest_service.domain.MessageEvent
import com.rest_service.dto.MessageDTO
import com.rest_service.enums.MessageEventType
import com.rest_service.event.MessageActionEvent
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.UserRepository
import com.rest_service.util.MessageUtil
import com.rest_service.util.SecurityUtil
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Singleton
class MessageService(
    private val securityUtil: SecurityUtil,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val messageEventRepository: MessageEventRepository,
    private val messageUtil: MessageUtil,
    private val applicationEventPublisher: ApplicationEventPublisher<MessageActionEvent>
) {

    fun list(roomId: UUID): Flux<MessageDTO> {
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
                val roomMemberIds = result.t2.map { it.userId }

                if (user.id !in roomMemberIds)
                    return@flatMap Flux.error(IncorrectInputException("User with id ${user.id} is not member of room with id $roomId"))

                messageEventRepository.findProjectionMessage(roomId, user.id)
                    .flatMap {
                        messageUtil.rehydrateMessage(it.messageId)
                            .map { messageResultReader ->
                                messageResultReader.toDto(user)
                            }
                    }
            }
    }

    fun create(command: MessageCommand): Mono<MessageDTO> {
        val email = securityUtil.getUserEmail()

        return Mono.zip(
            userRepository.findByEmail(email),
            memberRepository.findByRoomId(command.roomId)
                .switchIfEmpty(Flux.error(NotFoundException("Room with id ${command.roomId} doesn't exist.")))
                .collectList()
        )
            .flatMap { result ->
                val user = result.t1
                val roomMemberIds = result.t2.map { it.userId }

                if (user.id !in roomMemberIds)
                    return@flatMap Mono.error(IncorrectInputException("User with id ${user.id} is not member of room with id ${command.roomId}"))

                val messageEvent = MessageEvent(
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

                messageEventRepository.save(messageEvent)
                    .flatMap {
                        messageUtil.rehydrateMessage(it.messageId)
                            .map { messageResultReader ->

                                roomMemberIds.forEach { memberId ->
                                    val event = MessageActionEvent(memberId, messageResultReader)
                                    applicationEventPublisher.publishEventAsync(event)
                                }

                                messageResultReader.toDto(user)
                            }
                    }
            }
    }
}
