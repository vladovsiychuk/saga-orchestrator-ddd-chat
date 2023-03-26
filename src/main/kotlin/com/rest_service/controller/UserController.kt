package com.rest_service.controller

import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.dto.UserDTO
import com.rest_service.enums.UserType
import com.rest_service.service.UserService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Controller("/v1/users")
class UserController(private val service: UserService) {

    @Secured("isAuthenticated()")
    @Get("/{?listCommand*}")
    @Produces(MediaType.APPLICATION_JSON)
    fun index(listCommand: ListCommand): Flux<UserDTO> {
        return service.list(listCommand)
    }

    @Secured("isAuthenticated()")
    @Get("/currentUser")
    @Produces(MediaType.APPLICATION_JSON)
    fun getCurrentUser(): Mono<UserDTO> {
        return service.getCurrentUser()
    }

    @Secured("isAuthenticated()")
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun get(id: UUID): Mono<UserDTO> {
        return service.get(id)
    }

    @Secured("isAuthenticated()")
    @Post("/currentUser")
    @Produces(MediaType.APPLICATION_JSON)
    fun create(command: UserCommand): Mono<UserDTO> {
        if (command.type == UserType.TRANSLATOR) {
            command.translationLanguages = command.translationLanguages ?: mutableSetOf()
            command.translationLanguages!!.add(command.primaryLanguage)
        }

        return service.create(command)
    }
}
