package com.rest_service.publisher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.event.ActionEvent
import com.rest_service.event.MessageActionEvent
import com.rest_service.event.RoomActionEvent
import com.rest_service.event.websocket.MessageWebSocketEvent
import com.rest_service.event.websocket.RoomWebSocketEvent
import com.rest_service.websocket.WebSocketService
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class MessageListener(
    private val webSocketService: WebSocketService,
) {
    private val mapper = jacksonObjectMapper()


    @EventListener
    @Async
    open fun messageActionListener(event: ActionEvent) {
        when (event) {
            is MessageActionEvent -> messageActionListener(event)
            is RoomActionEvent    -> roomActionListener(event)
        }
    }

    open fun messageActionListener(event: MessageActionEvent) {
        val messageDTO = event.message
        val webSocketEvent = MessageWebSocketEvent(messageDTO)

        val message = mapper.writeValueAsString(webSocketEvent)

        webSocketService.sendMessageToUser(message, event.userId)
    }

    open fun roomActionListener(event: RoomActionEvent) {
        val webSocketEvent = RoomWebSocketEvent(event.room)

        val message = mapper.writeValueAsString(webSocketEvent)

        webSocketService.sendMessageToUser(message, event.userId)
    }
}
