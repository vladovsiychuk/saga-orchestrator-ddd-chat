package com.rest_service.manager

import com.rest_service.domain.RoomDomain
import com.rest_service.domain.UserDomain
import com.rest_service.entity.Member
import com.rest_service.entity.Room
import com.rest_service.event.ActionEvent
import com.rest_service.event.MessageActionEvent
import com.rest_service.event.RoomActionEvent
import com.rest_service.exception.NotFoundException
import com.rest_service.exception.UnauthorizedException
import com.rest_service.repository.MemberRepository
import com.rest_service.repository.MessageEventRepository
import com.rest_service.repository.RoomRepository
import com.rest_service.resultReader.MessageResultReader
import io.micronaut.context.event.ApplicationEventPublisher
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Singleton
class RoomManager(
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val messageEventRepository: MessageEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher<ActionEvent>,
    private val userManager: UserManager,
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
        }.switchIfEmpty(Mono.error(NotFoundException("Room with id $roomId doesn't exist.")))
    }

    fun listByUser(user: UserDomain): Flux<RoomDomain> {
        return memberRepository.findByUserId(user.toDto().id)
            .flatMap { this.findById(it.roomId, withMessages = true) }
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
                ).flatMap { _ -> this.findById(createdRoom.id) }
            }
    }

    fun addNewMember(room: RoomDomain, newMemberUser: UserDomain): Mono<RoomDomain> {
        val roomId = room.toDto().id
        val newMember = Member(roomId = roomId, userId = newMemberUser.toDto().id)

        return memberRepository.save(newMember)
            .flatMap { this.findById(roomId) }
    }

    fun broadcastMessageToRoomMembers(updatedRoom: RoomDomain): Mono<Boolean> {
        return Flux.fromIterable(updatedRoom.toDto().members)
            .map { memberId ->
                val event = RoomActionEvent(memberId, updatedRoom.toDto())
                applicationEventPublisher.publishEventAsync(event)
            }
            .collectList()
            .map { true }
    }

    fun broadcastMessageToRoomMembers(room: RoomDomain, message: MessageResultReader): Mono<Boolean> {
        return Flux.fromIterable(room.toDto().members)
            .flatMap { userManager.findByUserId(it) }
            .map { user ->
                val messageDto = message.toDomain(user).toDto()
                val messageEvent = MessageActionEvent(user.toDto().id, messageDto)
                applicationEventPublisher.publishEventAsync(messageEvent)
            }
            .collectList()
            .map { true }
    }

    fun validateUserIsRoomMember(user: UserDomain, room: RoomDomain): Mono<Boolean> {
        if (!room.isRoomMember(user))
            return Mono.error(UnauthorizedException())

        return Mono.just(true);
    }
}
