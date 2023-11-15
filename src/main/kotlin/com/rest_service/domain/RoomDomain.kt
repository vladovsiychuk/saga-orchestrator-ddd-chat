package com.rest_service.domain

import com.rest_service.dto.MessageProjectionDTO
import com.rest_service.dto.RoomDTO
import com.rest_service.entity.Member
import com.rest_service.entity.Room
import java.util.UUID

class RoomDomain(
    private val id: UUID,
    private val name: String?,
    private val createdBy: UUID,
    private val members: List<UUID>,
    val messages: List<UUID>,
    private val dateCreated: Long,
    private val dateUpdated: Long,
) {
    constructor(room: Room, members: List<Member>, messages: List<MessageProjectionDTO> = emptyList()) : this(
        room.id!!,
        room.name,
        room.createdBy,
        members.map { it.userId },
        messages.map { it.messageId },
        room.dateCreated,
        room.dateUpdated,
    )

    fun isRoomMember(user: UserDomain): Boolean {
        return user.toDto().id in members
    }

    fun hasAMessage(): Boolean {
        return messages.isNotEmpty()
    }

    fun createdByUser(user: UserDomain): Boolean {
        return user.toDto().id == createdBy
    }

    fun takeLastMessageIds(roomLimit: Int): List<UUID> {
        return messages.takeLast(roomLimit)
    }

    fun toDto(): RoomDTO {
        return RoomDTO(
            id,
            name,
            createdBy,
            members,
            dateCreated,
            dateUpdated
        )
    }
}
