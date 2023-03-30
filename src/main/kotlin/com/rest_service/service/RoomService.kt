package com.rest_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.command.RoomCommand
import com.rest_service.domain.Member
import com.rest_service.domain.Room
import com.rest_service.dto.RoomDTO
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MemberRepository
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
) {
    private val mapper = jacksonObjectMapper()
    private val logger: Logger = LoggerFactory.getLogger(RoomService::class.java)

    fun list(): Flux<RoomDTO> {
        val email = securityUtil.getUserEmail()

        return userRepository.findByEmail(email)
            .switchIfEmpty(Mono.error(NotFoundException("User with email $email was not found.")))
            .flux()
            .flatMap {
                memberRepository.findByUserId(it.id)
                    .flatMap { member ->
                        roomRepository.findById(member.roomId)
                            .map { room ->
                                mapper.convertValue(room, RoomDTO::class.java)
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

                val room = Room(currentUser.id)

                roomRepository.save(room)
                    .flatMap { createdRoom ->
                        val firstMember = Member(createdRoom.id, currentUser.id)
                        val secondMember = Member(createdRoom.id, companionUser.id)

                        Mono.zip(
                            memberRepository.save(firstMember),
                            memberRepository.save(secondMember)
                        )
                            .map {
                                mapper.convertValue(createdRoom, RoomDTO::class.java)
                            }
                    }
                    .doOnSuccess {
                        logger.info("New chat between ${currentUser.email} and ${companionUser.email} created.")
                    }
            }
    }
}
