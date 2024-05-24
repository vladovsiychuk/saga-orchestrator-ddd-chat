package com.saga_orchestrator_ddd_chat.saga_orchestrator.web

import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.MessageCreateRequest
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.MessageTranslateRequest
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.MessageUpdateRequest
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.service.MessageService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import reactor.core.publisher.Mono

@Controller("/v1/messages")
@Secured(SecurityRule.IS_AUTHENTICATED)
class MessageController(private val messageService: MessageService) {
    @Post("/")
    fun create(@Body request: MessageCreateRequest): Mono<ResponseDTO> {
        return messageService.startCreateMessage(request)
    }

    @Put("/{messageId}")
    fun update(@Body command: MessageUpdateRequest, messageId: UUID): Mono<ResponseDTO> {
        return messageService.update(command, messageId)
    }

    @Put("/{id}/read")
    fun read(id: UUID): Mono<ResponseDTO> {
        return messageService.read(id)
    }

    @Put("/{id}/translate")
    fun translate(@Body command: MessageTranslateRequest, id: UUID): Mono<ResponseDTO> {
        return messageService.translate(command, id)
    }
}
