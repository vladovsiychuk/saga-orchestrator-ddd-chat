package com.rest_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.dto.ChatDTO
import com.rest_service.dto.MessageDTO
import com.rest_service.repository.MessageRepository
import com.rest_service.repository.UserRepository
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import reactor.core.publisher.Flux

@Singleton
class MessageService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val securityService: SecurityService
) {
    private val mapper = jacksonObjectMapper()

    fun get(): Flux<ChatDTO> {
        val email = securityService.authentication.get().attributes["email"].toString()

        return userRepository.findByEmail(email)
            .flux()
            .flatMap { user ->
                messageRepository.findBySenderOrReceiverOrderByDateDesc(user.id, user.id)
                    .collectMultimap {
                        if (it.sender != user.id) it.sender else it.receiver
                    }.flatMapIterable {
                        it.entries
                    }.map {
                        val messages = it.value.map { message ->
                            mapper.convertValue(message, MessageDTO::class.java)
                        }.toList()

                        ChatDTO(it.key, messages)
                    }
            }
    }
}
