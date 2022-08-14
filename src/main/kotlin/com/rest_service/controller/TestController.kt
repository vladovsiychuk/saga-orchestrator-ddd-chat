package com.rest_service.controller

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import javax.annotation.security.PermitAll

@Controller("/test")
class TestController {
    @PermitAll
    @Get("/permitAll")
    @Produces(MediaType.APPLICATION_JSON)
    fun index(): String {
        return "test permitt all"
    }

    @Secured("isAuthenticated()")
    @Get("/secured")
    @Produces(MediaType.APPLICATION_JSON)
    fun index2(): String {
        return "test secured"
    }
}
