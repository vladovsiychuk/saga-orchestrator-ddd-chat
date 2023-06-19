package com.rest_service.service

import com.rest_service.command.RoomCommand
import com.rest_service.domain.Member
import com.rest_service.domain.Room
import com.rest_service.dto.RoomDTO
import com.rest_service.enums.MessageEventType
import com.rest_service.event.RoomActionEvent
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.exception.UnauthorizedException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.repository.UserRepository
import com.rest_service.util.SecurityUtil
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Singleton
class RoomService(
    private val securityUtil: SecurityUtil,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val roomRepository: RoomRepository,
    private val messageEventRepository: MessageEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<RoomActionEvent>,
) {
    private val logger: Logger = LoggerFactory.getLogger(RoomService::class.java)

    fun get(roomId: UUID): Mono<RoomDTO> {
        val email = securityUtil.getUserEmail()

        return userRepository.findByEmail(email)
            .flatMap { user ->
                memberRepository.findByRoomId(roomId)
                    .map { it.userId }
                    .collectList()
                    .flatMap { members ->
                        if (user.id !in members)
                            return@flatMap Mono.error(IncorrectInputException("User with id ${user.id} is not a member of room with id $roomId"))

                        roomRepository.findById(roomId)
                            .map {
                                RoomDTO(it, members)
                            }
                    }
            }
    }

    fun list(): Flux<RoomDTO> {
        val email = securityUtil.getUserEmail()

        return userRepository.findByEmail(email)
            .flux()
            .flatMap { currentUser ->

                memberRepository.findByUserId(currentUser.id!!)
                    .flatMap { member ->

                        roomRepository.findById(member.roomId)
                            .flatMap { room ->

                                messageEventRepository.existsByTypeAndRoomId(MessageEventType.MESSAGE_NEW, room.id!!)
                                    .flatMap { exist ->

                                        if (exist || room.createdBy == currentUser.id) { // user's rooms without messages
                                            memberRepository.findByRoomId(room.id)
                                                .map { it.userId }
                                                .collectList()
                                                .flatMap { roomMembers ->

                                                    Mono.just(RoomDTO(room, roomMembers))
                                                }
                                        } else
                                            Mono.empty()
                                    }
                            }
                    }
            }
    }

    fun create(command: RoomCommand): Mono<RoomDTO> {
        val email = securityUtil.getUserEmail()

        return Mono.zip(
            userRepository.findByEmail(email),
            userRepository.findById(command.userId)
                .switchIfEmpty(Mono.error(NotFoundException("User with id ${command.userId} does not exist.")))
        )
            .flatMap { result ->
                val currentUser = result.t1
                val companionUser = result.t2

                val room = Room(createdBy = currentUser.id!!)

                roomRepository.save(room)
                    .flatMap { createdRoom ->
                        val firstMember = Member(roomId = createdRoom.id!!, userId = currentUser.id)
                        val secondMember = Member(roomId = createdRoom.id, userId = companionUser.id!!)

                        Mono.zip(
                            memberRepository.save(firstMember),
                            memberRepository.save(secondMember)
                        )
                            .map {
                                RoomDTO(createdRoom, listOf(currentUser.id, companionUser.id))
                            }
                    }
                    .doOnSuccess {
                        logger.info("New chat between ${currentUser.email} and ${companionUser.email} created.")
                    }
            }
    }

    fun addMember(roomId: UUID, command: RoomCommand): Mono<RoomDTO> {
        val email = securityUtil.getUserEmail()

        return Mono.zip(
            memberRepository.findByRoomId(roomId).collectList(),
            roomRepository.findById(roomId)
                .switchIfEmpty(Mono.error(NotFoundException("Room with id $roomId does not exist."))),
            userRepository.findById(command.userId)
                .switchIfEmpty(Mono.error(NotFoundException("User with id ${command.userId} does not exist."))),
            userRepository.findByEmail(email)
        )
            .flatMap { result ->
                val members = result.t1
                val room = result.t2
                val currentUser = result.t4


                if (currentUser.id != room.createdBy)
                    return@flatMap Mono.error(UnauthorizedException())
                else if (members.map { it.userId }.contains(command.userId))
                    return@flatMap Mono.error(IncorrectInputException("User with id ${command.userId} is already a room member."))


                val newMember = Member(roomId = roomId, userId = command.userId)

                memberRepository.save(newMember)
                    .flatMap {

                        get(roomId)
                            .map { roomDTO ->

                                broadcastMessageToRoomMembers(roomDTO, roomDTO.members)

                                roomDTO
                            }
                    }
            }
    }

    private fun broadcastMessageToRoomMembers(
        roomDTO: RoomDTO,
        roomMemberIds: List<UUID>
    ) {
        roomMemberIds.forEach { memberId ->
            val event = RoomActionEvent(memberId, roomDTO)
            applicationEventPublisher.publishEventAsync(event)
        }
    }
}
