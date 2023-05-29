package com.rest_service.controller

import com.rest_service.command.MessageCommand
import com.rest_service.dto.MessageDTO
import com.rest_service.service.MessageService
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller("/v1/messages")
@Secured(SecurityRule.IS_AUTHENTICATED)
class MessageController(private val service: MessageService) {

    @Get("/")
    fun list(@QueryValue("roomLimit") roomLimit: Int): Flux<MessageDTO> {
        return service.list(roomLimit)
    }

    @Get("/rooms/{roomId}")
    fun getByRoom(roomId: UUID): Flux<MessageDTO> {
        return service.getRoomMessages(roomId)
    }

    @Post("/")
    fun create(@Body command: MessageCommand): Mono<MessageDTO> {
        return service.create(command)
    }

    @Put("/{id}/read")
    fun read(id: UUID): Mono<MessageDTO> {
        return service.read(id)
    }
}
