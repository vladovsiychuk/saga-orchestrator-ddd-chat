package com.rest_service.controller

import com.rest_service.command.RoomCommand
import com.rest_service.dto.RoomDTO
import com.rest_service.service.RoomService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller("/v1/rooms")
@Secured(SecurityRule.IS_AUTHENTICATED)
class RoomController(private val service: RoomService) {

    @Get("/")
    fun index(): Flux<RoomDTO> {
        return service.list()
    }

    @Post("/")
    fun create(command: RoomCommand): Mono<RoomDTO> {
        return service.create(command)
    }
}
