package com.rest_service.service

import com.rest_service.command.RoomCommand
import com.rest_service.dto.RoomDTO
import com.rest_service.entity.Member
import com.rest_service.event.RoomActionEvent
import com.rest_service.exception.IncorrectInputException
import com.rest_service.exception.NotFoundException
import com.rest_service.exception.UnauthorizedException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.repository.UserRepository
import com.rest_service.util.RoomUtil
import com.rest_service.util.SecurityUtil
import com.rest_service.util.UserUtil
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class RoomService(
    private val securityUtil: SecurityUtil,
    private val userRepository: UserRepository,
    private val memberRepository: MemberRepository,
    private val roomRepository: RoomRepository,
    private val messageEventRepository: MessageEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<RoomActionEvent>,
    private val roomUtil: RoomUtil,
    private val userUtil: UserUtil,
) {
    private val logger: Logger = LoggerFactory.getLogger(RoomService::class.java)

    fun get(roomId: UUID): Mono<RoomDTO> {
        return Mono.zip(
            userUtil.getCurrentUser(),
            roomUtil.findById(roomId)
        ) { user, room ->
            if (room.userIsMember(user)) room.toDto()
            else throw IncorrectInputException("User with id ${user.toDto().id} is not a member of room with id ${room.toDto().id}")
        }
    }

    fun list(): Flux<RoomDTO> {
        return userUtil.getCurrentUser()
            .flux()
            .flatMap { currentUser ->
                roomUtil.listByUser(currentUser)
                    .filter { it.hasAMessage() || it.createdByUser(currentUser) }
            }.map { roomDomain -> roomDomain.toDto() }
    }

    fun create(command: RoomCommand): Mono<RoomDTO> {
        return Mono.zip(
            userUtil.getCurrentUser(),
            userUtil.findByUserId(command.userId)
                .switchIfEmpty(Mono.error(NotFoundException("User with id ${command.userId} does not exist."))),
        ) { currentUser, companionUser ->
            roomUtil.createRoom(currentUser, companionUser)
        }.flatMap { it.map { room -> room.toDto() } }
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
