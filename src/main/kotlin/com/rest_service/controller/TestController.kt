package com.rest_service.controller

import com.rest_service.websocket.WebSocketService
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.security.annotation.Secured
import java.util.UUID

@Controller("/test")
class TestController(private val webSocketService: WebSocketService) {

    @Secured("isAuthenticated()")
    @Post("/")
    fun create(): String {
        webSocketService.sendMessageToUser("hello user", UUID.fromString("824d060e-43f2-41bd-814e-478467c16011"))
        return "works"
    }
}
