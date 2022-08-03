package com.rest_service.controller

import com.rest_service.dto.MessageDTO
import com.rest_service.service.MessageService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import reactor.core.publisher.Flux
import javax.annotation.security.PermitAll

@Controller("/v1/messages")
class MessageController(private val service: MessageService) {
    @PermitAll
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun index(): Flux<MessageDTO> {
        return service.get()
    }
}
