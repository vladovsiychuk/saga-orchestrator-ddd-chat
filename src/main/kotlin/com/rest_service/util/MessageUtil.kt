package com.rest_service.util

import com.rest_service.domain.User
import com.rest_service.dto.MessageDTO
import com.rest_service.repository.MessageEventRepository
import com.rest_service.resultReader.MessageResultReader
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import java.util.UUID

@Singleton
class MessageUtil(private val eventRepository: MessageEventRepository) {

    fun rehydrateMessage(messageId: UUID, user: User): Mono<MessageDTO> {
        return eventRepository.findByMessageIdOrderByDateCreated(messageId)
            .collectList()
            .map { events ->
                val resultReader = MessageResultReader(messageId, user)

                events.forEach {
                    resultReader.apply(it)
                }

                resultReader.toDto()
            }
    }
}
