package com.saga_orchestrator_ddd_chat.commons.client

import com.saga_orchestrator_ddd_chat.commons.dto.MessageDTO
import com.saga_orchestrator_ddd_chat.commons.dto.RoomDTO
import com.saga_orchestrator_ddd_chat.commons.dto.UserDTO
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ViewServiceFetcher {
    fun getUser(id: UUID): Mono<UserDTO>
    fun getCurrentUser(): Mono<UserDTO>
    fun getRoom(id: UUID): Mono<RoomDTO>
    fun getMessage(id: UUID): Mono<MessageDTO>
    fun getMessagesByRoomId(roomId: UUID): Flux<MessageDTO>
}
