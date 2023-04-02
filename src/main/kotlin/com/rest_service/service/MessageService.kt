package com.rest_service.service

import com.rest_service.dto.MessageDTO
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.UserRepository
import com.rest_service.util.MessageUtil
import com.rest_service.util.SecurityUtil
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Singleton
class MessageService(
    private val securityUtil: SecurityUtil,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val messageEventRepository: MessageEventRepository,
    private val messageUtil: MessageUtil,
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
                        messageUtil.rehydrateMessage(it.messageId, user)
                    }
            }
    }
}
