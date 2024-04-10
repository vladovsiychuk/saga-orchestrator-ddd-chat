package com.rest_service.read_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.dto.UserDTO
import com.rest_service.read_service.SecurityManager
import com.rest_service.read_service.entity.UserView
import com.rest_service.read_service.exception.NotFoundException
import com.rest_service.read_service.repository.RoomMembersRepository
import com.rest_service.read_service.repository.RoomViewRepository
import com.rest_service.read_service.repository.UserViewRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
class UserService(
    private val securityManager: SecurityManager,
    private val userViewRepository: UserViewRepository,
    private val roomMemberRepository: RoomMembersRepository,
    private val roomViewRepository: RoomViewRepository,
) {
    val mapper = jacksonObjectMapper()

    fun updateUser(user: UserDTO) {
        val userEntity = mapper.convertValue(user, UserView::class.java)

        userViewRepository.findById(userEntity.id)
            .flatMap { userViewRepository.update(userEntity) }
            .switchIfEmpty { userViewRepository.save(userEntity) }
            .subscribe()
    }

    fun get(userId: UUID): Mono<UserDTO> {
        return userViewRepository.findById(userId)
            .map { mapper.convertValue(it, UserDTO::class.java) }
            .switchIfEmpty(NotFoundException("User with id $userId does not exist.").toMono())
    }

    fun getCurrentUser(): Mono<UserDTO> {
        TODO("Not yet implemented")
    }

    fun getAllUsers(): Flux<UserDTO> {
        return securityManager.getCurrentUserEmail().toMono()
            .flatMap { currentUserEmail -> userViewRepository.findByEmail(currentUserEmail) }
            .flatMapMany { currentUser ->
                roomMemberRepository.findByMemberId(currentUser.id) // find user's room ids
                    .flatMap { roomViewRepository.findById(it.roomId) }
                    .flatMap { room -> room.members.toFlux() }
                    .distinct()
                    .filter { it != currentUser.id }
                    .flatMap { userId -> userViewRepository.findById(userId) }
                    .map { mapper.convertValue(it, UserDTO::class.java) }
            }
    }
}
