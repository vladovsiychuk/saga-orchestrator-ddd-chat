package com.rest_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.domain.User
import com.rest_service.dto.UserDTO
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.UserRepository
import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Singleton
class UserService(private val userRepository: UserRepository, private val securityService: SecurityService) {
    private val mapper = jacksonObjectMapper()

    fun get(): Mono<UserDTO> {
        val email = securityService.authentication.get().attributes["email"].toString()

        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(NotFoundException("User with email $email was not found.")))
            .map {
                mapper.convertValue(it, UserDTO::class.java)
            }
    }

    fun get(id: UUID): Mono<UserDTO> {
        return userRepository.findById(id)
            .map {
                mapper.convertValue(it, UserDTO::class.java)
            }
    }

    fun create(): Mono<UserDTO> {
        val email = securityService.authentication.get().attributes["email"].toString()

        return userRepository.findByEmail(email)
            .hasElement()
            .flatMap {
                if (it) {
                    return@flatMap Mono.error(NotFoundException("User with email $email already exist."))
                }

                val now = Instant.now()
                    .toEpochMilli()

                userRepository.save(
                    User(
                        UUID.randomUUID(),
                        email,
                        now,
                        now
                    )
                )
                    .map { user ->
                        mapper.convertValue(user, UserDTO::class.java)
                    }
            }
    }
}
