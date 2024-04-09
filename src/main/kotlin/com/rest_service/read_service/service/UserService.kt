package com.rest_service.read_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.dto.UserDTO
import com.rest_service.read_service.entity.UserView
import com.rest_service.read_service.exception.NotFoundException
import com.rest_service.read_service.repository.UserViewRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Singleton
class UserService(private val repository: UserViewRepository) {

    val mapper = jacksonObjectMapper()

    fun updateUser(user: UserDTO) {
        val userEntity = mapper.convertValue(user, UserView::class.java)

        repository.findById(userEntity.id)
            .flatMap { repository.update(userEntity) }
            .switchIfEmpty { repository.save(userEntity) }
            .subscribe()
    }

    fun get(userId: UUID): Mono<UserDTO> {
        return repository.findById(userId)
            .map { mapper.convertValue(it, UserDTO::class.java) }
            .switchIfEmpty(NotFoundException("User with id $userId does not exist.").toMono())
    }

    fun getCurrentUser(): Mono<UserDTO> {
        TODO("Not yet implemented")
    }
}
