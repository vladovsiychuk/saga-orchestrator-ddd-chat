package com.saga_orchestrator_ddd_chat.read_service.controller

import com.saga_orchestrator_ddd_chat.commons.dto.RoomDTO
import com.saga_orchestrator_ddd_chat.read_service.service.RoomService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller("/v1/rooms")
@Secured(SecurityRule.IS_AUTHENTICATED)
class RoomViewController(private val roomService: RoomService) {

    @Get("/")
    fun list(): Flux<RoomDTO> {
        return roomService.list()
    }

    @Get("/{id}")
    fun get(id: UUID): Mono<RoomDTO> {
        return roomService.get(id)
    }
}
