package com.rest_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.domain.User
import com.rest_service.dto.UserDTO
import com.rest_service.enums.UserType
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.UserRepository
import com.rest_service.util.MessageUtil
import com.rest_service.util.SecurityUtil
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Singleton
class UserService(
    private val repository: UserRepository,
    private val securityUtil: SecurityUtil,
    private val messageUtil: MessageUtil,
) {

    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    private val mapper = jacksonObjectMapper()

    fun getCurrentUser(): Mono<UserDTO> {
        val email = securityUtil.getUserEmail()

        return repository.findByEmail(email)
            .switchIfEmpty(Mono.error(NotFoundException("User with email $email was not found.")))
            .map {
                mapper.convertValue(it, UserDTO::class.java)
            }
    }

    fun get(id: UUID): Mono<UserDTO> {
        return repository.findById(id)
            .map {
                mapper.convertValue(it, UserDTO::class.java)
            }
    }

    fun create(command: UserCommand): Mono<UserDTO> {
        if (command.type == UserType.TRANSLATOR && command.translationLanguages!!.size < 2)
            return Mono.error(IncorrectInputException("A translator user must have at least 1 translation language."))
        else if (command.type == UserType.REGULAR_USER && !command.translationLanguages.isNullOrEmpty())
            return Mono.error(IncorrectInputException("A regular user cannot have translation languages."))


        val user = User(command, securityUtil)

        return repository.save(user)
            .map {
                logger.info("User with email $it.email was created.")

                mapper.convertValue(it, UserDTO::class.java)
            }
    }

    fun list(listCommand: ListCommand): Flux<UserDTO> {
        return if (listCommand.roomLimit != null)
            searchAllUsers(listCommand)
        else
            searchByQuery(listCommand)
    }

    private fun searchAllUsers(listCommand: ListCommand): Flux<UserDTO> {
        val currentUserEmail = securityUtil.getUserEmail()

        return messageUtil.findLastMessagesPerRoom(listCommand.roomLimit!!)
            .flatMap {

                Flux.fromIterable(it.read + it.senderId)
                    .flatMap { userId ->

                        repository.findById(userId)
                            .map { user ->
                                mapper.convertValue(user, UserDTO::class.java)
                            }
                    }
            }
            .filter { it.email != currentUserEmail }
            .distinct { it.id }
    }

    private fun searchByQuery(listCommand: ListCommand): Flux<UserDTO> =
        repository.findByEmailIlike("%${listCommand.query}%")
            .map {
                mapper.convertValue(it, UserDTO::class.java)
            }
}
