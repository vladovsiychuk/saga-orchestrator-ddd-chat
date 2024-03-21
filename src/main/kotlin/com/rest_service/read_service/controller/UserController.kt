package com.rest_service.read_service.controller

import com.rest_service.commons.dto.UserDTO
import com.rest_service.read_service.service.UserService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import reactor.core.publisher.Mono

@Controller("/v1/users")
@Secured(SecurityRule.IS_AUTHENTICATED)
class UserController(private val userService: UserService) {
    @Get("/{id}")
    fun get(id: UUID): Mono<UserDTO> {
        return userService.get(id)
    }
}
