package com.rest_service.controller

import com.rest_service.websocket.WebSocketService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

@Controller("/test")
class TestController(private val webSocketService: WebSocketService) {

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/")
    fun create(@Body body: Map<*, *>): MyClass {
        return MyClass("a", "b", listOf("A", "B"), body["bodyValue"].toString())
    }
}

data class MyClass(
    val firstParam: String,
    val secondParam: String,
    val listParam: List<String>,
    val bodyValue: String,
)
