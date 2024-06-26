package com.saga_orchestrator_ddd_chat.mock

import com.saga_orchestrator_ddd_chat.commons.client.ViewServiceFetcher
import com.saga_orchestrator_ddd_chat.commons.dto.MessageDTO
import com.saga_orchestrator_ddd_chat.commons.dto.RoomDTO
import com.saga_orchestrator_ddd_chat.commons.dto.UserDTO
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.retry.annotation.Fallback
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Fallback
@Requires(env = [Environment.TEST])
class MockedViewClient : ViewServiceFetcher {
    override fun getUser(id: UUID): Mono<UserDTO> {
        TODO("Not yet implemented")
    }

    override fun getCurrentUser(): Mono<UserDTO> {
        TODO("Not yet implemented")
    }

    override fun getRoom(id: UUID): Mono<RoomDTO> {
        TODO("Not yet implemented")
    }

    override fun getMessage(id: UUID): Mono<MessageDTO> {
        TODO("Not yet implemented")
    }

    override fun getMessagesByRoomId(roomId: UUID): Flux<MessageDTO> {
        TODO("Not yet implemented")
    }
}
