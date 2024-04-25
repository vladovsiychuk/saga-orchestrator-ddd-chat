package com.rest_service.messaging.room.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.RoomAddMemberCommand
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.messaging.room.infrastructure.RoomDomainEvent
import com.rest_service.messaging.room.infrastructure.RoomDomainEventType
import java.util.UUID

class Room : Domain {
    private var status = RoomStatus.IN_CREATION
    private lateinit var room: RoomData

    private val mapper = jacksonObjectMapper()
    fun apply(event: RoomDomainEvent): DomainEvent {
        when (event.type) {
            RoomDomainEventType.ROOM_CREATED               -> handleRoomCreated(event)
            RoomDomainEventType.ROOM_MEMBER_ADDED          -> handleRoomMemberAdded(event)
            RoomDomainEventType.MESSAGE_CREATE_APPROVED    -> checkForRoomCreatedStatus()
            RoomDomainEventType.MESSAGE_READ_APPROVED,
            RoomDomainEventType.MESSAGE_TRANSLATE_APPROVED -> {
                checkForRoomCreatedStatus()
                checkMembership(event)
            }

            else                                           -> {}
        }

        return event
    }

    private fun handleRoomCreated(event: RoomDomainEvent) {
        checkForRoomInCreationStatus()
        val command = mapper.convertValue(event.payload, RoomCreateCommand::class.java)

        room = RoomData(
            event.roomId,
            null,
            event.responsibleUserId,
            mutableSetOf(command.companionId, event.responsibleUserId),
            event.dateCreated,
            event.dateCreated,
        )

        status = RoomStatus.CREATED
    }

    private fun handleRoomMemberAdded(event: RoomDomainEvent) {
        checkForRoomCreatedStatus()
        val command = mapper.convertValue(event.payload, RoomAddMemberCommand::class.java)
        room.members.add(command.memberId)
    }

    private fun checkForRoomCreatedStatus() {
        if (status != RoomStatus.CREATED)
            throw RuntimeException("Room is not yet created.")
    }

    private fun checkForRoomInCreationStatus() {
        if (status != RoomStatus.IN_CREATION)
            throw RuntimeException("Room is already created.")
    }

    private fun checkMembership(event: RoomDomainEvent) {
        if (event.responsibleUserId !in room.members)
            throw RuntimeException("User with id ${event.responsibleUserId} is not a member of the room with id ${room.id}")
    }

    override fun toDto(): RoomDTO {
        return RoomDTO(
            room.id,
            room.name,
            room.createdBy,
            room.members,
            room.dateCreated,
            room.dateUpdated,
        )
    }

    private data class RoomData(
        val id: UUID,
        val name: String?,
        val createdBy: UUID,
        val members: MutableSet<UUID>,
        val dateCreated: Long,
        val dateUpdated: Long,
    )

    private enum class RoomStatus {
        IN_CREATION,
        CREATED,
    }
}
