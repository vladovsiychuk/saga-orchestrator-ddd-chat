package com.rest_service.websocket_service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.client.ViewServiceFetcher
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.websocket_service.configuration.WebSocketService
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class SagaEventHandler(
    private val webSocketService: WebSocketService,
    private val viewServiceFetcher: ViewServiceFetcher
) {

    val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_COMPLETED    -> handleUserCreate(event)

            SagaEventType.ROOM_ADD_MEMBER_COMPLETED,
            SagaEventType.ROOM_CREATE_COMPLETED    -> handleRoomUpdate(event)

            SagaEventType.MESSAGE_CREATE_COMPLETED,
            SagaEventType.MESSAGE_READ_COMPLETED,
            SagaEventType.MESSAGE_TRANSLATE_COMPLETED,
            SagaEventType.MESSAGE_UPDATE_COMPLETED -> handleMessageUpdate(event)

            else                                   -> {}
        }
    }

    private fun handleUserCreate(event: SagaEvent) {
        mapper.convertValue(event.payload, UserDTO::class.java)
            .let { user ->
                WebSocketEvent(user, WebSocketType.USER_CREATED)
                    .let { event -> mapper.writeValueAsString(event) }
                    .let { eventString -> webSocketService.sendMessageToUser(eventString, user.temporaryId!!) }
            }
    }

    private fun handleMessageUpdate(event: SagaEvent) {
        mapper.convertValue(event.payload, MessageDTO::class.java)
            .let { message ->
                Mono.zip(
                    viewServiceFetcher.getRoom(message.roomId),
                    WebSocketEvent(message, WebSocketType.MESSAGE_UPDATED).toMono()
                ) { room, event ->
                    mapper.writeValueAsString(event)
                        .let { eventJsonString ->
                            room.members.forEach { roomMemberId ->
                                webSocketService.sendMessageToUser(eventJsonString, roomMemberId)
                            }
                        }
                }
            }
    }

    private fun handleRoomUpdate(event: SagaEvent) {
        mapper.convertValue(event.payload, RoomDTO::class.java)
            .let { room ->
                WebSocketEvent(room, WebSocketType.ROOM_UPDATED)
                    .let { mapper.writeValueAsString(it) }
                    .let { eventJsonString ->
                        room.members.forEach { roomMemberId ->
                            webSocketService.sendMessageToUser(eventJsonString, roomMemberId)
                        }
                    }
            }
    }
}
