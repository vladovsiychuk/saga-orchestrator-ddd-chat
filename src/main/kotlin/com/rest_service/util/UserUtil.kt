package com.rest_service.util

import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.domain.UserDomain
import com.rest_service.entity.User
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.UserRepository
import com.rest_service.service.UserService
import jakarta.inject.Singleton
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class UserUtil(
    private val securityUtil: SecurityUtil,
    private val repository: UserRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)

    fun getCurrentUser(): Mono<UserDomain> {
        val email = securityUtil.getUserEmail()
        return repository.findByEmail(email)
            .map { UserDomain(it) }
    }

    fun findByUserId(userId: UUID): Mono<UserDomain> {
        return repository.findById(userId)
            .map { UserDomain(it) }
            .switchIfEmpty(Mono.error(NotFoundException("User with id $userId does not exist.")))
    }

    fun createUser(command: UserCommand): Mono<UserDomain> {
        val user = User(
            username = command.username,
            email = securityUtil.getUserEmail(),
            primaryLanguage = command.primaryLanguage,
            translationLanguages = command.translationLanguages?.map { it.toString() },
            type = command.type,
        )

        return repository.save(user)
            .map {
                logger.info("User with email $it.email was created.")

                UserDomain(it)
            }
    }

    fun list(listCommand: ListCommand): Flux<UserDomain> {
        return repository.findByTypeAndEmail(listCommand.type, "%${listCommand.query}%")
            .map { UserDomain(it) }
    }
}
