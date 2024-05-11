package com.saga_orchestrator_ddd_chat.commons.client

import com.saga_orchestrator_ddd_chat.commons.dto.MessageDTO
import com.saga_orchestrator_ddd_chat.commons.dto.RoomDTO
import com.saga_orchestrator_ddd_chat.commons.dto.UserDTO
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import io.micronaut.retry.annotation.CircuitBreaker
import io.micronaut.retry.annotation.Recoverable
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Client(id = "view-service")
@Requires(notEnv = [Environment.TEST])
@CircuitBreaker(attempts = "2", delay = "200ms")
@Recoverable(api = ViewServiceFetcher::class)
interface ViewServiceClient : ViewServiceFetcher {

    @Get("/internal/users/{id}")
    override fun getUser(id: UUID): Mono<UserDTO>

    @Get("/internal/users/currentUser")
    override fun getCurrentUser(): Mono<UserDTO>

    @Get("/internal/rooms/{id}")
    override fun getRoom(id: UUID): Mono<RoomDTO>

    @Get("/internal/messages/{id}")
    override fun getMessage(id: UUID): Mono<MessageDTO>

    @Get("/internal/messages/rooms/{roomId}")
    override fun getMessagesByRoomId(roomId: UUID): Flux<MessageDTO>
}
