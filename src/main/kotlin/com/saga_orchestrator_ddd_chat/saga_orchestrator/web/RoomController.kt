package com.saga_orchestrator_ddd_chat.saga_orchestrator.web

import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.RoomAddMemberRequest
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.request.RoomCreateRequest
import com.saga_orchestrator_ddd_chat.saga_orchestrator.web.service.RoomService
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import reactor.core.publisher.Mono

@Controller("/v1/rooms")
@Secured(SecurityRule.IS_AUTHENTICATED)
class RoomController(private val roomService: RoomService) {
    @Post("/")
    fun create(request: RoomCreateRequest): Mono<ResponseDTO> {
        return roomService.startCreateRoom(request)
    }

    @Put("/{roomId}/members")
    fun addMember(roomId: UUID, @Body command: RoomAddMemberRequest): Mono<ResponseDTO> {
        return roomService.startAddMember(roomId, command)
    }
}
