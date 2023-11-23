package com.rest_service.service

import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.dto.UserDTO
import com.rest_service.util.RoomUtil
import com.rest_service.util.UserUtil
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class UserService(
    private val userUtil: UserUtil,
    private val roomUtil: RoomUtil,
) {
    fun getCurrentUser(): Mono<UserDTO> {
        return userUtil.getCurrentUser().map { it.toDto() }
    }

    fun get(id: UUID): Mono<UserDTO> {
        return userUtil.findByUserId(id).map { it.toDto() }
    }

    fun create(command: UserCommand): Mono<UserDTO> {
        return command.validate()
            .flatMap { userUtil.createUser(command) }
            .map { it.toDto() }
    }

    fun list(listCommand: ListCommand): Flux<UserDTO> {
        return userUtil.list(listCommand)
            .map { it.toDto() }
    }

    fun getMembersOfUserRooms(): Flux<UserDTO> {
        return userUtil.getCurrentUser()
            .flux()
            .flatMap { currentUser ->
                roomUtil.listByUser(currentUser)
                    .map { it.toDto() }
                    .flatMap { Flux.fromIterable(it.members) }
                    .distinct()
                    .filter { it != currentUser.toDto().id }
            }
            .flatMap { userUtil.findByUserId(it) }
            .map { it.toDto() }
    }
}
