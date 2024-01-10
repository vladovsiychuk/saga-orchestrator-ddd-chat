package com.rest_service.service

import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.domain.RoomDomain
import com.rest_service.domain.UserDomain
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
        return userManager.getCurrentUser().map(UserDomain::toDto)
    }

    fun get(id: UUID): Mono<UserDTO> {
        return userManager.findByUserId(id).map(UserDomain::toDto)
    }

    fun create(command: UserCommand): Mono<UserDTO> {
        return command.validate()
            .flatMap { userManager.createUser(command) }
            .map(UserDomain::toDto)
    }

    fun list(listCommand: ListCommand): Flux<UserDTO> {
        return userManager.list(listCommand)
            .map(UserDomain::toDto)
    }

    fun getMembersOfUserRooms(): Flux<UserDTO> {
        return userManager.getCurrentUser()
            .flatMapMany { currentUser ->
                roomManager.listByUser(currentUser)
                    .map(RoomDomain::toDto)
                    .flatMapIterable { it.members }
                    .distinct()
                    .filter { it != currentUser.toDto().id }
            }
            .flatMap { userManager.findByUserId(it) }
            .map(UserDomain::toDto)
    }
}
