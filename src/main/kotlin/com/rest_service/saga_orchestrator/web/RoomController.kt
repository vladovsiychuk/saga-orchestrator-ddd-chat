package com.rest_service.saga_orchestrator.web

import com.rest_service.saga_orchestrator.web.request.RoomCreateRequest
import com.rest_service.saga_orchestrator.web.service.RoomService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Mono

@Controller("/v1/rooms")
@Secured(SecurityRule.IS_AUTHENTICATED)
class RoomController(private val roomService: RoomService) {
    @Post("/")
    fun create(request: RoomCreateRequest): Mono<ResponseDTO> {
        return roomService.startCreateRoom(request)
    }
}
