package com.rest_service.controller

import com.rest_service.command.MessageCommand
import com.rest_service.dto.MessageDTO
import com.rest_service.service.MessageService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Controller("/v1/messages")
@Secured(SecurityRule.IS_AUTHENTICATED)
class MessageController(private val service: MessageService) {

    @Get("/")
    fun index(@QueryValue roomId: UUID): Flux<MessageDTO> {
        return service.list(roomId)
    }

    @Post("/")
    fun create(@Body command: MessageCommand): Mono<MessageDTO> {
        return service.create(command)
    }
}
