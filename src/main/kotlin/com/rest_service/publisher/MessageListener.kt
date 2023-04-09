package com.rest_service.publisher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.event.MessageActionEvent
import com.rest_service.repository.UserRepository
import com.rest_service.websocket.WebSocketService
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class MessageListener(
    private val userRepository: UserRepository,
    private val webSocketService: WebSocketService,
) {
    private val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: MessageActionEvent) {
        userRepository.findById(event.userId)
            .subscribe {
                val message = mapper.writeValueAsString(event.message.toDto(it))

                webSocketService.sendMessageToUser(message, event.userId)
            }
    }
}
