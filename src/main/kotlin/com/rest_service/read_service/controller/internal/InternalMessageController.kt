package com.rest_service.read_service.controller.internal

import com.rest_service.commons.dto.MessageDTO
import com.rest_service.read_service.service.MessageService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.util.UUID
import javax.annotation.security.PermitAll
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller("/internal/messages")
@PermitAll
class InternalMessageController(private val messageService: MessageService) {
    @Get("/{id}")
    fun get(id: UUID): Mono<MessageDTO> {
        return messageService.get(id)
    }

    @Get("/rooms/{roomId}")
    fun getMessagesByRoomId(roomId: UUID): Flux<MessageDTO> {
        return messageService.getMessagesByRoomId(roomId)
    }
}
