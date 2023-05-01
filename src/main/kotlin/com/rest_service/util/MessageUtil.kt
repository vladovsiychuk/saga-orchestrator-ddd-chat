package com.rest_service.util

import com.rest_service.dto.MessageDTO
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.UserRepository
import com.rest_service.resultReader.MessageResultReader
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Singleton
class MessageUtil(
    private val eventRepository: MessageEventRepository,
    private val securityUtil: SecurityUtil,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val messageEventRepository: MessageEventRepository,
) {

    fun findLastMessagesPerRoom(roomLimit: Int): Flux<MessageDTO> {
        val email = securityUtil.getUserEmail()

        return userRepository.findByEmail(email)
            .flux()
            .flatMap { user ->

                // find rooms user is a member
                memberRepository.findByUserId(user.id)
                    .flatMap {

                        messageEventRepository.findProjectionMessageWithLimit(it.roomId, roomLimit)
                            .flatMap { messageProjection ->

                                rehydrateMessage(messageProjection.messageId)
                                    .map { messageRR ->
                                        messageRR.toDto(user)
                                    }
                            }
                    }
            }
    }

    fun rehydrateMessage(messageId: UUID): Mono<MessageResultReader> {
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
