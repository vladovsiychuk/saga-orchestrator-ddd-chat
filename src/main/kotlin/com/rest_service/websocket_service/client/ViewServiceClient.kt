package com.rest_service.websocket_service.client

import com.rest_service.commons.dto.RoomDTO
import com.rest_service.commons.dto.UserDTO
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.CircuitBreaker
import io.micronaut.retry.annotation.Recoverable
import java.util.UUID
import reactor.core.publisher.Mono

@Client(id = "view_service")
@Requires(notEnv = [Environment.TEST])
@CircuitBreaker(attempts = "2", delay = "200ms")
@Recoverable(api = ViewServiceFetcher::class)
interface ViewServiceClient : ViewServiceFetcher {

    @Get("/internal/users/{id}")
    override fun getUser(id: UUID): Mono<UserDTO>

    @Get("/internal/users/{id}")
    override fun getRoom(id: UUID): Mono<RoomDTO>
}
