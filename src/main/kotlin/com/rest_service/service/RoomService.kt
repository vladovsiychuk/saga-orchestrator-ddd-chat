package com.rest_service.service

import com.rest_service.command.RoomCommand
import com.rest_service.dto.RoomDTO
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.UnauthorizedException
import com.rest_service.manager.RoomManager
import com.rest_service.manager.UserManager
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class RoomService(
    private val roomManager: RoomManager,
    private val userManager: UserManager,
) {
    fun get(roomId: UUID): Mono<RoomDTO> {
        return Mono.zip(
            userManager.getCurrentUser(),
            roomManager.findById(roomId)
        ) { user, room ->
            roomManager.validateUserIsRoomMember(user, room)
                .map { room.toDto() }
        }.flatMap { it }
    }

    fun list(): Flux<RoomDTO> {
        return userManager.getCurrentUser()
            .flux()
            .flatMap { currentUser ->
                roomManager.listByUser(currentUser)
                    .filter { it.hasAMessage() || it.createdByUser(currentUser) }
            }.map { roomDomain -> roomDomain.toDto() }
    }

    fun create(command: RoomCommand): Mono<RoomDTO> {
        return Mono.zip(
            userManager.getCurrentUser(),
            userManager.findByUserId(command.userId)
        ) { currentUser, companionUser ->
            roomManager.createRoom(currentUser, companionUser)
        }.flatMap { it.map { room -> room.toDto() } }
    }

    fun addMember(roomId: UUID, command: RoomCommand): Mono<RoomDTO> {
        return Mono.zip(
            userManager.getCurrentUser(),
            userManager.findByUserId(command.userId),
            roomManager.findById(roomId)
        ).flatMap {
            val currentUser = it.t1
            val newMemberUser = it.t2
            val room = it.t3

            if (!room.createdByUser(currentUser))
                return@flatMap Mono.error(UnauthorizedException())
            else if (room.isRoomMember(newMemberUser))
                return@flatMap Mono.error(IncorrectInputException("User with id ${command.userId} is already a room member."))

            roomManager.addNewMember(room, newMemberUser)
                .flatMap { updatedRoom ->
                    Mono.zip(
                        roomManager.broadcastMessageToRoomMembers(updatedRoom),
                        Mono.just(updatedRoom)
                    ) { _, room -> room.toDto() }
                }
        }
    }
}
