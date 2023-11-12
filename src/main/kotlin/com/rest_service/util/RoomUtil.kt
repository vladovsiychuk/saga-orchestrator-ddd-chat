package com.rest_service.util

import com.rest_service.domain.RoomDomain
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.RoomRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono

@Singleton
class RoomUtil(
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
) {
    fun findById(roomId: UUID): Mono<RoomDomain> {
        return Mono.zip(
            roomRepository.findById(roomId),
            memberRepository.findByRoomId(roomId).collectList()
        ).map { result ->
            val room = result.t1
            val members = result.t2

            RoomDomain(room, members)
        }
    }
}
