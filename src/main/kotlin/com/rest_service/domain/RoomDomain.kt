package com.rest_service.domain

import com.rest_service.dto.RoomDTO
import com.rest_service.entity.Member
import com.rest_service.entity.Room
import com.rest_service.exception.IncorrectInputException
import java.util.UUID
import reactor.core.publisher.Mono

class RoomDomain(
    private val id: UUID,
    private val name: String?,
    private val createdBy: UUID,
    private val members: List<UUID>,
    private val dateCreated: Long,
    private val dateUpdated: Long,
) {
    constructor(room: Room, members: List<Member>) : this(
        room.id!!,
        room.name,
        room.createdBy,
        members.map { it.userId },
        room.dateCreated,
        room.dateUpdated,
    )

    fun userIsMember(userId: UUID): Mono<Boolean> {
        return Mono.just(userId in members)
            .flatMap {
                if (it == false)
                    return@flatMap Mono.error(IncorrectInputException("User with id $userId is not a member of room with id $id"))
                else
                    Mono.just(it)
            }
    }

    fun toDTO(): Mono<RoomDTO> {
        return Mono.just(
            RoomDTO(
                id,
                name,
                createdBy,
                members,
                dateCreated,
                dateUpdated
            )
        )
    }
}
