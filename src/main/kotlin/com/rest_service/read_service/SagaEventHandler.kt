package com.rest_service.read_service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.MessageDTO
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.read_service.service.MessageService
import com.rest_service.read_service.service.RoomService
import com.rest_service.read_service.service.UserService
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class SagaEventHandler(
    private val userService: UserService,
    private val roomService: RoomService,
    private val messageService: MessageService,
) {

    val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_COMPLETED    -> handleUserCreate(event)

            SagaEventType.ROOM_ADD_MEMBER_COMPLETED,
            SagaEventType.ROOM_CREATE_COMPLETED    -> handleRoomModify(event)

            SagaEventType.MESSAGE_CREATE_COMPLETED,
            SagaEventType.MESSAGE_READ_COMPLETED,
            SagaEventType.MESSAGE_TRANSLATE_COMPLETED,
            SagaEventType.MESSAGE_UPDATE_COMPLETED -> handleMessageModify(event)

            else                                   -> {}
        }
    }

    private fun handleMessageModify(event: SagaEvent) {
        messageService.updateMessage(mapper.convertValue(event.payload, MessageDTO::class.java))
    }

    private fun handleRoomModify(event: SagaEvent) {
        roomService.updateRoom(mapper.convertValue(event.payload, RoomDTO::class.java))
    }

    private fun handleUserCreate(event: SagaEvent) {
        userService.updateUser(mapper.convertValue(event.payload, UserDTO::class.java))
    }
}
