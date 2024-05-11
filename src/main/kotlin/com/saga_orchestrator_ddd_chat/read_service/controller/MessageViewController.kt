package com.saga_orchestrator_ddd_chat.read_service.controller

import com.saga_orchestrator_ddd_chat.commons.dto.MessageDTO
import com.saga_orchestrator_ddd_chat.read_service.service.MessageService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Flux

@Controller("/v1/messages")
@Secured(SecurityRule.IS_AUTHENTICATED)
class MessageViewController(private val messageService: MessageService) {
    @Get("/")
    fun list(@QueryValue("roomLimit") roomLimit: Int): Flux<MessageDTO> {
        return messageService.list(roomLimit)
    }
}
