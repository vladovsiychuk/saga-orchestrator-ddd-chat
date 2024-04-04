package com.rest_service.saga_orchestrator.web

import com.rest_service.saga_orchestrator.web.request.UserCreateRequest
import com.rest_service.saga_orchestrator.web.service.UserService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import reactor.core.publisher.Mono

@Controller("/v1/users")
@Secured(SecurityRule.IS_AUTHENTICATED)
class UserController(private val userService: UserService, ) {
    @Post("/currentUser")
    fun create(request: UserCreateRequest): Mono<ResponseDTO> {
        return userService.startCreateCurrentUser(request)
    }
}