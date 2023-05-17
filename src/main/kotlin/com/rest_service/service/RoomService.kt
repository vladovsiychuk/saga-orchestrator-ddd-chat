package com.rest_service.service

import com.rest_service.command.RoomCommand
import com.rest_service.domain.Member
import com.rest_service.domain.Room
import com.rest_service.dto.RoomDTO
import com.rest_service.enums.MessageEventType
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.repository.UserRepository
import com.rest_service.util.SecurityUtil
import jakarta.inject.Singleton
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
) {
    private val logger: Logger = LoggerFactory.getLogger(RoomService::class.java)

    fun list(): Flux<RoomDTO> {
        val email = securityUtil.getUserEmail()

        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(NotFoundException("User with email $email was not found.")))
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
}
