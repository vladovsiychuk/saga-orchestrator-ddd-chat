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
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(SagaEventHandler::class.java)

    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_COMPLETED     -> handleUserCreate(event)

            SagaEventType.ROOM_ADD_MEMBER_COMPLETED -> handleRoomMemberAdded(event)
            SagaEventType.ROOM_CREATE_COMPLETED     -> handleRoomCreated(event)

            SagaEventType.MESSAGE_CREATE_COMPLETED  -> handleMessageCreated(event)
            SagaEventType.MESSAGE_READ_COMPLETED,
            SagaEventType.MESSAGE_TRANSLATE_COMPLETED,
            SagaEventType.MESSAGE_UPDATE_COMPLETED  -> handleMessageUpdated(event)

            else                                    -> {}
        }
    }

    private fun handleUserCreate(event: SagaEvent) {
        val user = mapper.convertValue(event.payload, UserDTO::class.java)

        sendDtoToUser(user, user.id)
            .subscribe()
    }

    private fun handleMessageCreated(event: SagaEvent) {
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

    private fun handleMessageUpdated(event: SagaEvent) {
        val message = mapper.convertValue(event.payload, MessageDTO::class.java)

        viewServiceFetcher.getRoom(message.roomId)
            .flatMapMany { room ->
                room.members.toFlux()
                    .flatMap { roomMemberId -> sendDtoToUser(message, roomMemberId) }
            }.subscribe()
    }

    private fun handleRoomCreated(event: SagaEvent) {
        val room = mapper.convertValue(event.payload, RoomDTO::class.java)

        sendDtoToUser(room, room.createdBy)
            .zipWith(
                room.members.toFlux()
                    .filter { it != room.createdBy }
                    .single()
                    .flatMap { viewServiceFetcher.getUser(it) }
                    .flatMap { sendDtoToUser(it, room.createdBy) }
            )
            .map { true }
            .subscribe()
    }

    private fun handleRoomMemberAdded(event: SagaEvent) {
        val room = mapper.convertValue(event.payload, RoomDTO::class.java)

        room.members.toFlux()
            .flatMap { viewServiceFetcher.getUser(it) }
            .flatMap { roomMember ->
                room.members.toFlux()
                    .flatMap { roomMemberId ->
                        if (roomMemberId != roomMember.id) sendDtoToUser(roomMember, roomMemberId)
                        else true.toMono()
                    }
            }
            .thenMany(
                viewServiceFetcher.getMessagesByRoomId(room.id)
                    .flatMap { message ->
                        room.members.toFlux()
                            .flatMap { sendDtoToUser(message, it) }
                    }
            )
            .thenMany(
                room.members.toFlux()
                    .flatMap { sendDtoToUser(room, it) }
            )
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
                .map {
                    logger.info("Message: $it. Sent to: $userId")
                    webSocketService.sendMessageToUser(it, userId)
                }
                .thenReturn(true)
        }
    }
}
