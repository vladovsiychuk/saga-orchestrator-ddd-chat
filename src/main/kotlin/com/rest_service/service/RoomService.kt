package com.rest_service.service

import com.rest_service.command.RoomCommand
import com.rest_service.dto.RoomDTO
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.UnauthorizedException
import com.rest_service.util.RoomUtil
import com.rest_service.util.UserUtil
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class RoomService(
    private val roomUtil: RoomUtil,
    private val userUtil: UserUtil,
) {
    fun get(roomId: UUID): Mono<RoomDTO> {
        return Mono.zip(
            userUtil.getCurrentUser(),
            roomUtil.findById(roomId)
        ) { user, room ->
            roomUtil.validateUserIsRoomMember(user, room)
                .map { room.toDto() }
        }.flatMap { it }
    }

    fun list(): Flux<RoomDTO> {
        return userUtil.getCurrentUser()
            .flux()
            .flatMap { currentUser ->
                roomUtil.listByUser(currentUser)
                    .filter { it.hasAMessage() || it.createdByUser(currentUser) }
            }.map { roomDomain -> roomDomain.toDto() }
    }

    fun create(command: RoomCommand): Mono<RoomDTO> {
        return Mono.zip(
            userUtil.getCurrentUser(),
            userUtil.findByUserId(command.userId)
        ) { currentUser, companionUser ->
            roomUtil.createRoom(currentUser, companionUser)
        }.flatMap { it.map { room -> room.toDto() } }
    }

    fun addMember(roomId: UUID, command: RoomCommand): Mono<RoomDTO> {
        return Mono.zip(
            userUtil.getCurrentUser(),
            userUtil.findByUserId(command.userId),
            roomUtil.findById(roomId)
        ).flatMap {
            val currentUser = it.t1
            val newMemberUser = it.t2
            val room = it.t3

            if (!room.createdByUser(currentUser))
                return@flatMap Mono.error(UnauthorizedException())
            else if (room.isRoomMember(newMemberUser))
                return@flatMap Mono.error(IncorrectInputException("User with id ${command.userId} is already a room member."))

            roomUtil.addNewMember(room, newMemberUser)
                .flatMap { updatedRoom ->
                    Mono.zip(
                        roomUtil.broadcastMessageToRoomMembers(updatedRoom),
                        Mono.just(updatedRoom)
                    ) { _, room -> room.toDto() }
                }
        }
    }
}
