package com.rest_service.util

import com.rest_service.repository.MessageEventRepository
import com.rest_service.resultReader.MessageResultReader
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import java.util.UUID

@Singleton
class MessageUtil(private val eventRepository: MessageEventRepository) {

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
