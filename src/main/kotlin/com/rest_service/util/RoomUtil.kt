package com.rest_service.util

import com.rest_service.domain.RoomDomain
import com.rest_service.domain.UserDomain
import com.rest_service.entity.Member
import com.rest_service.entity.Room
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.RoomRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class RoomUtil(
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val messageEventRepository: MessageEventRepository,
) {
    fun findById(roomId: UUID, withMessages: Boolean = false): Mono<RoomDomain> {
        return Mono.zip(
            roomRepository.findById(roomId),
            memberRepository.findByRoomId(roomId).collectList(),
            if (withMessages) messageEventRepository.findProjectionByRoomId(roomId).collectList() else Mono.just(listOf())
        ).map { result ->
            val room = result.t1
            val members = result.t2
            val messages = result.t3

            RoomDomain(room, members, messages)
        }
    }

    fun listByUser(user: UserDomain): Flux<RoomDomain> {
        return memberRepository.findByUserId(user.toDto().id)
            .flatMap { findById(it.roomId, withMessages = true) }
    }

    fun createRoom(currentUserDomain: UserDomain, companionUserDomain: UserDomain): Mono<RoomDomain> {
        val currentUser = currentUserDomain.toDto()
        val companionUser = companionUserDomain.toDto()

        val room = Room(createdBy = currentUser.id)

        return roomRepository.save(room)
            .flatMap { createdRoom ->
                val firstMember = Member(roomId = createdRoom.id!!, userId = currentUser.id)
                val secondMember = Member(roomId = createdRoom.id, userId = companionUser.id)

                Mono.zip(
                    memberRepository.save(firstMember),
                    memberRepository.save(secondMember)
                ) { _, _ ->
                    RoomDomain(createdRoom, listOf(firstMember, secondMember))
                }
            }
    }
}
