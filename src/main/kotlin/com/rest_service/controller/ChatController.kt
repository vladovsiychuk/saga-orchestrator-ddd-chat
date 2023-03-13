package com.rest_service.controller

import com.rest_service.dto.ChatDTO
import com.rest_service.service.ChatService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import reactor.core.publisher.Flux

@Controller("/v1/chats")
class ChatController(private val service: ChatService) {
    @Secured("isAuthenticated()")
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun index(): Flux<ChatDTO> {
        return service.get()
    }
}
