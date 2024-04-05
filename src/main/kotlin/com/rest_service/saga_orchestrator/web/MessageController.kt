package com.rest_service.saga_orchestrator.web

import com.rest_service.saga_orchestrator.web.request.MessageCreateRequest
import com.rest_service.saga_orchestrator.web.service.MessageService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Mono

@Controller("/v1/messages")
@Secured(SecurityRule.IS_AUTHENTICATED)
class MessageController(private val messageService: MessageService) {
    @Post("/")
    fun create(request: MessageCreateRequest): Mono<ResponseDTO> {
        return messageService.startCreateRoom(request)
    }
}
