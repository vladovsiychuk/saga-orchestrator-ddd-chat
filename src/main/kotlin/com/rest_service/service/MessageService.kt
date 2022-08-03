package com.rest_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.dto.MessageDTO
import com.rest_service.repository.MessageRepository
import jakarta.inject.Singleton
import reactor.core.publisher.Flux

@Singleton
class MessageService (private val repository: MessageRepository) {
    private val mapper = jacksonObjectMapper()

    fun get(): Flux<MessageDTO> {
//        return repository.findBySenderNameOrReceiverName(userName,userName)
        return repository.findAll()
            .map {
                mapper.convertValue(it, MessageDTO::class.java)
            }
    }
}
