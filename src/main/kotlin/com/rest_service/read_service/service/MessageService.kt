package com.rest_service.read_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.read_service.entity.MessageView
import com.rest_service.read_service.exception.NotFoundException
import com.rest_service.read_service.repository.MessageViewRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Singleton
class MessageService(private val repository: MessageViewRepository) {

    val mapper = jacksonObjectMapper()

    fun updateMessage(message: MessageDTO) {
        val messageEntity = mapper.convertValue(message, MessageView::class.java)

        repository.findById(messageEntity.id)
            .flatMap { repository.update(messageEntity) }
            .switchIfEmpty { repository.save(messageEntity) }
            .subscribe()
    }

    fun get(messageId: UUID): Mono<MessageDTO> {
        return repository.findById(messageId)
            .map { mapper.convertValue(it, MessageDTO::class.java) }
            .switchIfEmpty(NotFoundException("Message with id $messageId does not exist.").toMono())
    }

    fun getMessagesByRoomId(roomId: UUID): Flux<MessageDTO> {
        return repository.findByRoomId(roomId)
            .map { mapper.convertValue(it, MessageDTO::class.java) }
    }
}
