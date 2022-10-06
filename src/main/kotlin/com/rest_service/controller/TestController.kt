package com.rest_service.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller("/test")
class TestController {

    @Post("/")
    fun create(data: String) {
        println(data)
    }
}
