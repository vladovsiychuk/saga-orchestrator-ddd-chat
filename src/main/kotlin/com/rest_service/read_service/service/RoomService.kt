package com.rest_service.read_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.read_service.entity.RoomMember
import com.rest_service.read_service.entity.RoomView
import com.rest_service.read_service.exception.NotFoundException
import com.rest_service.read_service.repository.RoomMembersRepository
import com.rest_service.read_service.repository.RoomViewRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono

@Singleton
class RoomService(
    private val repository: RoomViewRepository,
    private val memberRepository: RoomMembersRepository,
) {

    val mapper = jacksonObjectMapper()

    fun updateRoom(room: RoomDTO) {
        val roomEntity = mapper.convertValue(room, RoomView::class.java)

        repository.findById(roomEntity.id)
            .flatMap { repository.update(roomEntity) }
            .switchIfEmpty { repository.save(roomEntity) }
            .then(updateMembers(room))
            .subscribe()
    }

    fun get(roomId: UUID): Mono<RoomDTO> {
        return repository.findById(roomId)
            .map { mapper.convertValue(it, RoomDTO::class.java) }
            .switchIfEmpty(NotFoundException("Room with id $roomId does not exist.").toMono())
    }

    private fun updateMembers(room: RoomDTO): Mono<Void> {
        return memberRepository.deleteByRoomId(room.id)
            .flatMapMany { room.members.toFlux() }
            .flatMap { memberId -> memberRepository.save(RoomMember(roomId = room.id, memberId = memberId)) }
            .then()
    }
}
