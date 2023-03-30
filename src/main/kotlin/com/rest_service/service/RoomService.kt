package com.rest_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.dto.RoomDTO
import com.rest_service.exception.NotFoundException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.repository.UserRepository
import com.rest_service.util.SecurityUtil
import jakarta.inject.Singleton
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
}
