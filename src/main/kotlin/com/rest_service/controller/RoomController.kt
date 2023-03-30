package com.rest_service.controller

import com.rest_service.dto.RoomDTO
import com.rest_service.service.RoomService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import reactor.core.publisher.Flux

@Controller("/v1/rooms")
class RoomController(private val service: RoomService) {

    @Secured("isAuthenticated()")
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun index(): Flux<RoomDTO> {
        return service.list()
    }
}
