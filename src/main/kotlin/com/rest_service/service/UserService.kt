package com.rest_service.service

import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.dto.UserDTO
import com.rest_service.manager.RoomManager
import com.rest_service.manager.UserManager
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class UserService(
    private val userManager: UserManager,
    private val roomManager: RoomManager,
) {
    fun getCurrentUser(): Mono<UserDTO> {
        return userManager.getCurrentUser().map { it.toDto() }
    }

    fun get(id: UUID): Mono<UserDTO> {
        return userManager.findByUserId(id).map { it.toDto() }
    }

    fun create(command: UserCommand): Mono<UserDTO> {
        return command.validate()
            .flatMap { userManager.createUser(command) }
            .map { it.toDto() }
    }

    fun list(listCommand: ListCommand): Flux<UserDTO> {
        return userManager.list(listCommand)
            .map { it.toDto() }
    }

    fun getMembersOfUserRooms(): Flux<UserDTO> {
        return userManager.getCurrentUser()
            .flux()
            .flatMap { currentUser ->
                roomManager.listByUser(currentUser)
                    .map { it.toDto() }
                    .flatMap { Flux.fromIterable(it.members) }
                    .distinct()
                    .filter { it != currentUser.toDto().id }
            }
            .flatMap { userManager.findByUserId(it) }
            .map { it.toDto() }
    }
}
