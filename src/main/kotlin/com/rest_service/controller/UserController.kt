package com.rest_service.controller

import com.rest_service.dto.UserDTO
import com.rest_service.service.UserService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import reactor.core.publisher.Mono
import java.util.UUID

@Controller("/v1/user")
class UserController(private val service: UserService) {
    @Secured("isAuthenticated()")
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(): Mono<UserDTO> {
        return service.get()
    }

    @Secured("isAuthenticated()")
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(id: String): Mono<UserDTO> {
        return service.get(UUID.fromString(id))
    }

    @Secured("isAuthenticated()")
    @Post("/")
    @Produces(MediaType.APPLICATION_JSON)
    fun create(): Mono<UserDTO> {
        return service.create()
    }
}
