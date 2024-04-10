package com.rest_service.read_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.read_service.exception.NotFoundException
import com.rest_service.read_service.repository.RoomViewRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
class InternalRoomService(
    private val roomViewRepository: RoomViewRepository,
) {
    val mapper = jacksonObjectMapper()

    fun get(roomId: UUID): Mono<RoomDTO> {
        return roomViewRepository.findById(roomId)
            .map { mapper.convertValue(it, RoomDTO::class.java) }
            .switchIfEmpty(NotFoundException("Room with id $roomId does not exist.").toMono())
    }
}
