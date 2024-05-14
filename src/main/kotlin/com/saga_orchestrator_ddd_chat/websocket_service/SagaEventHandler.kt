package com.saga_orchestrator_ddd_chat.websocket_service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.client.ViewServiceFetcher
import com.saga_orchestrator_ddd_chat.commons.dto.DTO
import com.saga_orchestrator_ddd_chat.commons.dto.MessageDTO
import com.saga_orchestrator_ddd_chat.commons.dto.RoomDTO
import com.saga_orchestrator_ddd_chat.commons.dto.UserDTO
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.websocket_service.configuration.WebSocketService
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

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
                    .let { eventString -> webSocketService.sendMessageToUser(eventString, user.id) }
            }
    }

    private fun handleMessageUpdate(event: SagaEvent) {
        val message = mapper.convertValue(event.payload, MessageDTO::class.java)

        Mono.zip(
            viewServiceFetcher.getRoom(message.roomId),
            viewServiceFetcher.getUser(message.senderId),
        ).flatMapMany { (room, sender) ->
            room.members.toFlux()
                .flatMap { roomMemberId ->
                    Mono.zip(
                        if (roomMemberId != message.senderId) sendDtoToUser(sender, roomMemberId) else true.toMono(),
                        if (roomMemberId != message.senderId) sendDtoToUser(room, roomMemberId) else true.toMono(),
                        sendDtoToUser(message, roomMemberId)
                    ).flatMapMany { true.toMono() }
                }
        }.subscribe()
    }

    private fun handleRoomUpdate(event: SagaEvent) {
        val room = mapper.convertValue(event.payload, RoomDTO::class.java)

        viewServiceFetcher.getMessagesByRoomId(room.id).collectList()
            .map { roomMessages ->
                WebSocketEvent(room, WebSocketType.ROOM_UPDATED)
                    .let { mapper.writeValueAsString(it) }
                    .let { eventJsonString ->
                        if (roomMessages.isEmpty())
                            webSocketService.sendMessageToUser(eventJsonString, room.createdBy)
                        else {
                            room.members.forEach { roomMemberId ->
                                webSocketService.sendMessageToUser(eventJsonString, roomMemberId)
                            }
                        }
                    }
            }
            .subscribe()
    }

    private fun sendDtoToUser(dto: DTO, userId: UUID): Mono<Boolean> {
        return when (dto) {
            is UserDTO    -> WebSocketEvent(dto, WebSocketType.USER_UPDATED)
            is RoomDTO    -> WebSocketEvent(dto, WebSocketType.ROOM_UPDATED)
            is MessageDTO -> WebSocketEvent(dto, WebSocketType.MESSAGE_UPDATED)
            else          -> throw UnsupportedOperationException()
        }.let { webSocketEvent ->
            mapper.writeValueAsString(webSocketEvent).toMono()
                .map { webSocketService.sendMessageToUser(it, userId) }
                .thenReturn(true)
        }
    }
}
