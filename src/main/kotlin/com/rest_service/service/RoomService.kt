package com.rest_service.service

import com.rest_service.command.RoomCommand
import com.rest_service.domain.RoomDomain
import com.rest_service.dto.RoomDTO
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.UnauthorizedException
import com.rest_service.manager.RoomManager
import com.rest_service.manager.UserManager
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.zip
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3

@Singleton
class RoomService(
    private val roomManager: RoomManager,
    private val userManager: UserManager,
) {
    fun get(roomId: UUID): Mono<RoomDTO> {
        return zip(
            userManager.getCurrentUser(),
            roomManager.findById(roomId)
        )
            .flatMap { (user, room) ->
                roomManager.validateUserIsRoomMember(user, room)
                    .thenReturn(room.toDto())
            }
    }

    fun list(): Flux<RoomDTO> {
        return userManager.getCurrentUser()
            .flatMapMany { currentUser ->
                roomManager.listByUser(currentUser)
                    .filter { it.hasAMessage() || it.createdByUser(currentUser) }
                    .map(RoomDomain::toDto)
            }
    }

    fun create(command: RoomCommand): Mono<RoomDTO> {
        return zip(
            userManager.getCurrentUser(),
            userManager.findByUserId(command.userId)
        )
            .flatMap { (currentUser, companionUser) ->
                roomManager.createRoom(currentUser, companionUser)
                    .map(RoomDomain::toDto)
            }
    }

    fun addMember(roomId: UUID, command: RoomCommand): Mono<RoomDTO> {
        return zip(
            userManager.getCurrentUser(),
            userManager.findByUserId(command.userId),
            roomManager.findById(roomId)
        )
            .flatMap { (currentUser, newMemberUser, room) ->
                when {
                    !room.createdByUser(currentUser) -> UnauthorizedException().toMono()
                    room.isRoomMember(newMemberUser) -> IncorrectInputException("User with id ${command.userId} is already a room member.").toMono()

                    else                             ->
                        roomManager.addNewMember(room, newMemberUser)
                            .flatMap { updatedRoom ->
                                roomManager.broadcastMessageToRoomMembers(updatedRoom)
                                    .thenReturn(updatedRoom.toDto())
                            }
                }
            }
    }
}
