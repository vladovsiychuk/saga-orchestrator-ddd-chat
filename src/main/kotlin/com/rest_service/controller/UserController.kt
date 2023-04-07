package com.rest_service.controller

import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.dto.UserDTO
import com.rest_service.enums.UserType
import com.rest_service.service.UserService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Controller("/v1/users")
@Secured(SecurityRule.IS_AUTHENTICATED)
class UserController(private val service: UserService) {

    @Get("/{?listCommand*}")
    fun index(listCommand: ListCommand): Flux<UserDTO> {
        return service.list(listCommand)
    }

    @Get("/currentUser")
    fun getCurrentUser(): Mono<UserDTO> {
        return service.getCurrentUser()
    }

    @Get("/{id}")
    fun get(id: UUID): Mono<UserDTO> {
        return service.get(id)
    }

    @Post("/currentUser")
    fun create(command: UserCommand): Mono<UserDTO> {
        if (command.type == UserType.TRANSLATOR) {
            command.translationLanguages = command.translationLanguages ?: mutableSetOf()
            command.translationLanguages!!.add(command.primaryLanguage)
        }

        return service.create(command)
    }
}
