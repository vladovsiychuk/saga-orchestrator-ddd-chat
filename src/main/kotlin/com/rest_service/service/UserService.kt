package com.rest_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.dto.UserDTO
import com.rest_service.entity.User
import com.rest_service.enums.UserType
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.UserRepository
import com.rest_service.util.SecurityUtil
import jakarta.inject.Singleton
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class UserService(
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val securityUtil: SecurityUtil,
) {

    private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    private val mapper = jacksonObjectMapper()

    fun getCurrentUser(): Mono<UserDTO> {
        val email = securityUtil.getUserEmail()

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

    fun create(command: UserCommand): Mono<UserDTO> {
        if (command.type == UserType.TRANSLATOR && command.translationLanguages!!.size < 2)
            return Mono.error(IncorrectInputException("A translator user must have at least 1 translation language."))
        else if (command.type == UserType.REGULAR_USER && !command.translationLanguages.isNullOrEmpty())
            return Mono.error(IncorrectInputException("A regular user cannot have translation languages."))


        val user = User(
            username = command.username,
            email = securityUtil.getUserEmail(),
            primaryLanguage = command.primaryLanguage,
            translationLanguages = command.translationLanguages?.map { it.toString() },
            type = command.type,
        )

        return userRepository.save(user)
            .map {
                logger.info("User with email $it.email was created.")

                mapper.convertValue(it, UserDTO::class.java)
            }
    }

    fun list(listCommand: ListCommand): Flux<UserDTO> {
        return userRepository.findByTypeAndEmail(listCommand.type, "%${listCommand.query}%")
            .map {
                mapper.convertValue(it, UserDTO::class.java)
            }
    }

    fun getMembersOfUserRooms(): Flux<UserDTO> {
        val email = securityUtil.getUserEmail()

        return userRepository.findByEmail(email)
            .flatMapMany { currentUser ->
                memberRepository.findByUserId(currentUser.id!!)
                    .map { member -> member.roomId }
                    .flatMap {
                        getRoomMembers(it)
                            .filter { user -> user.id != currentUser.id }
                    }.distinct()
            }
    }

    private fun getRoomMembers(roomId: UUID): Flux<UserDTO> =
        memberRepository.findByRoomId(roomId)
            .flatMap {

                userRepository.findById(it.userId)
                    .map {
                        mapper.convertValue(it, UserDTO::class.java)
                    }
            }
}
