package com.saga_orchestrator_ddd_chat.read_service.controller.internal

import com.saga_orchestrator_ddd_chat.commons.dto.RoomDTO
import com.saga_orchestrator_ddd_chat.read_service.service.InternalRoomService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import jakarta.annotation.security.PermitAll
import java.util.UUID
import reactor.core.publisher.Mono

@Controller("/internal/rooms")
@PermitAll
class InternalRoomController(private val internalRoomService: InternalRoomService) {
    @Get("/{id}")
    fun get(id: UUID): Mono<RoomDTO> {
        return internalRoomService.get(id)
    }
}
