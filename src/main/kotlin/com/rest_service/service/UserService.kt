package com.rest_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.command.ListCommand
import com.rest_service.command.UserCommand
import com.rest_service.domain.Room
import com.rest_service.domain.User
import com.rest_service.dto.UserDTO
import com.rest_service.enums.MessageEventType
import com.rest_service.enums.UserType
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.repository.UserRepository
import com.rest_service.util.MessageUtil
import com.rest_service.util.SecurityUtil
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Singleton
class UserService(
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val messageEventRepository: MessageEventRepository,
    private val roomRepository: RoomRepository,
    private val securityUtil: SecurityUtil,
    private val messageUtil: MessageUtil,
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
        return if (listCommand.roomLimit != null)
            searchUserRelatedUsers(listCommand)
        else
            searchByQuery(listCommand)
    }

    private fun searchUserRelatedUsers(listCommand: ListCommand): Flux<UserDTO> {
        val currentUserEmail = securityUtil.getUserEmail()

        return Mono.zip(
            usersFromLastMessages(listCommand, currentUserEmail),
            usersFromEmptyRooms(currentUserEmail)
        )
            .flux()
            .flatMap { result ->
                val usersFromMessages = result.t1
                val usersFromEmptyRooms = result.t2

                Flux.fromIterable((usersFromMessages + usersFromEmptyRooms).distinctBy { it.id })
            }
    }

    private fun usersFromEmptyRooms(currentUserEmail: String): Mono<List<UserDTO>> {
        return userRepository.findByEmail(currentUserEmail)
            .flatMap { currentUser ->

                findRoomsCreatedByUser(currentUser)
                    .flatMap { room ->

                        messageEventRepository.existsByTypeAndRoomId(
                            MessageEventType.MESSAGE_NEW,
                            room.id!!
                        )
                            .flux()
                            .flatMap { exist ->
                                if (exist)
                                    Flux.empty()
                                else
                                    getRoomMembers(room)
                                        .filter { it.id != currentUser.id }
                            }
                    }
                    .collectList()
            }
    }

    private fun getRoomMembers(room: Room): Flux<UserDTO> =
        memberRepository.findByRoomId(room.id!!)
            .flatMap {

                userRepository.findById(it.userId)
                    .map {
                        mapper.convertValue(it, UserDTO::class.java)
                    }
            }

    private fun findRoomsCreatedByUser(currentUser: User): Flux<Room> =
        memberRepository.findByUserId(currentUser.id!!)
            .flatMap { member ->

                roomRepository.findById(member.roomId)
                    .flux()
                    .filter { it.createdBy == currentUser.id }
            }


    private fun usersFromLastMessages(
        listCommand: ListCommand,
        currentUserEmail: String
    ): Mono<List<UserDTO>> =
        messageUtil.findLastMessagesPerRoom(listCommand.roomLimit!!)
            .groupBy { it.roomId }
            .flatMap { roomToMessages ->

                memberRepository.findByRoomId(roomToMessages.key())
                    .collectList()
                    .flux()
                    .flatMap { members ->
                        val isOneToOneChat = members.size == 2

                        roomToMessages
                            .collectList()
                            .flux()
                            .flatMap { messages ->
                                val messagesRelatedUsers =
                                    messages.flatMap { it.read + it.senderId + it.translatorId }

                                Flux.fromIterable((messagesRelatedUsers + if (isOneToOneChat) members.map { it.userId } else emptyList()).filterNotNull()
                                    .toSet())
                                    .flatMap { userId ->

                                        userRepository.findById(userId)
                                            .map { user ->
                                                mapper.convertValue(user, UserDTO::class.java)
                                            }
                                    }
                            }
                    }
            }
            .filter { it.email != currentUserEmail }
            .distinct { it.id }
            .collectList()

    private fun searchByQuery(listCommand: ListCommand): Flux<UserDTO> =
        userRepository.findByTypeAndEmail(listCommand.type, "%${listCommand.query}%")
            .map {
                mapper.convertValue(it, UserDTO::class.java)
            }
}
