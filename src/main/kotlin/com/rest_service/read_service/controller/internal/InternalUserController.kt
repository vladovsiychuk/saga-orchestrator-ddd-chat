package com.rest_service.read_service.controller.internal

import com.rest_service.commons.dto.UserDTO
import com.rest_service.read_service.service.UserService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import java.util.UUID
import javax.annotation.security.PermitAll
import reactor.core.publisher.Mono

@Controller("/internal/users")
@PermitAll
class InternalUserController(private val userService: UserService) {
    @Get("/{id}")
    fun get(id: UUID): Mono<UserDTO> {
        return userService.get(id)
    }

    @Get("/currentUser")
    fun getCurrentUser(): Mono<UserDTO> {
        return userService.getCurrentUser()
    }
}
