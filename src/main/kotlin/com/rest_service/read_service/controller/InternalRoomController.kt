package com.rest_service.read_service.controller

import com.rest_service.commons.dto.RoomDTO
import com.rest_service.read_service.service.RoomService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.util.UUID
import javax.annotation.security.PermitAll
import reactor.core.publisher.Mono

@Controller("/internal/rooms")
@PermitAll
class InternalRoomController(private val roomService: RoomService) {
    @Get("/{id}")
    fun get(id: UUID): Mono<RoomDTO> {
        return roomService.get(id)
    }
}
