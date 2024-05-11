package com.saga_orchestrator_ddd_chat.read_service.controller

import com.saga_orchestrator_ddd_chat.commons.dto.UserDTO
import com.saga_orchestrator_ddd_chat.read_service.ListCommand
import com.saga_orchestrator_ddd_chat.read_service.service.UserService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller("/v1/users")
@Secured(SecurityRule.IS_AUTHENTICATED)
class UserViewController(private val userService: UserService) {

    @Get("/all")
    fun roomsMembers(): Flux<UserDTO> {
        return userService.getAllUsers()
    }

    @Get("/{id}")
    fun get(id: UUID): Mono<UserDTO> {
        return userService.get(id)
    }

    @Get("/currentUser")
    fun getCurrentUser(): Mono<UserDTO> {
        return userService.getCurrentUser()
    }

    @Get("/{?listCommand*}")
    fun index(listCommand: ListCommand): Flux<UserDTO> {
        return userService.list(listCommand)
    }
}
