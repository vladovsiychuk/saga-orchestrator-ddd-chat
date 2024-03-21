package com.rest_service.read_service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.EventType
import com.rest_service.read_service.service.UserService
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton

@Singleton
open class SagaEventHandler(private val userService: UserService) {

    val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: DomainEvent) {
        when (event.type) {
            EventType.USER_CREATE_APPROVE -> handleUserCreate(event)
            EventType.ROOM_CREATE_APPROVE -> TODO()
            else                          -> {}
        }
    }

    private fun handleUserCreate(event: DomainEvent) {
        userService.updateUser(mapper.convertValue(event.payload, UserDTO::class.java))
    }
}
