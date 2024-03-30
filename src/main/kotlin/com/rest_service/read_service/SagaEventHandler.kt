package com.rest_service.read_service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.read_service.service.UserService
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class SagaEventHandler(private val userService: UserService) {

    val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.USER_CREATE_APPROVE -> handleUserCreate(event)
            SagaEventType.ROOM_CREATE_APPROVE -> TODO()
            else                              -> {}
        }
    }

    private fun handleUserCreate(event: SagaEvent) {
        userService.updateUser(mapper.convertValue(event.payload, UserDTO::class.java))
    }
}
