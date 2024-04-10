package com.rest_service.read_service.controller

import com.rest_service.commons.dto.MessageDTO
import com.rest_service.read_service.service.MessageService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller("/v1/messages")
@Secured(SecurityRule.IS_AUTHENTICATED)
class MessageController(private val messageService: MessageService) {
    @Get("/{id}")
    fun get(id: UUID): Mono<MessageDTO> {
        return messageService.get(id)
    }

    @Get("/")
    fun list(@QueryValue("roomLimit") roomLimit: Int): Flux<MessageDTO> {
        return messageService.list(roomLimit)
    }
}
