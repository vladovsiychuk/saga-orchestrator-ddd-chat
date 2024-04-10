package com.rest_service.read_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.dto.RoomDTO
import com.rest_service.read_service.SecurityManager
import com.rest_service.read_service.entity.RoomMember
import com.rest_service.read_service.entity.RoomView
import com.rest_service.read_service.exception.NotFoundException
import com.rest_service.read_service.exception.UnauthorizedException
import com.rest_service.read_service.repository.MessageViewRepository
import com.rest_service.read_service.repository.RoomMemberRepository
import com.rest_service.read_service.repository.RoomViewRepository
import com.rest_service.read_service.repository.UserViewRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux
import reactor.kotlin.core.publisher.toMono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Singleton
class RoomService(
    private val roomViewRepository: RoomViewRepository,
    private val memberRepository: RoomMemberRepository,
    private val securityManager: SecurityManager,
    private val userViewRepository: UserViewRepository,
    private val messageViewRepository: MessageViewRepository,
) {

    val mapper = jacksonObjectMapper()

    fun updateRoom(room: RoomDTO) {
        val roomEntity = mapper.convertValue(room, RoomView::class.java)

        roomViewRepository.findById(roomEntity.id)
            .flatMap { roomViewRepository.update(roomEntity) }
            .switchIfEmpty { roomViewRepository.save(roomEntity) }
            .then(updateMembers(room))
            .subscribe()
    }

    fun get(roomId: UUID): Mono<RoomDTO> {
        return Mono.zip(
            roomViewRepository.findById(roomId)
                .switchIfEmpty(NotFoundException("Room with id $roomId does not exist.").toMono()),
            userViewRepository.findByEmail(securityManager.getCurrentUserEmail()),
        )
            .flatMap { (room, currentUser) ->
                if (!room.members.contains(currentUser.id))
                    return@flatMap Mono.error(UnauthorizedException())

                mapper.convertValue(room, RoomDTO::class.java).toMono()
            }
    }

    private fun updateMembers(room: RoomDTO): Mono<Void> {
        return memberRepository.deleteByRoomId(room.id)
            .flatMapMany { room.members.toFlux() }
            .flatMap { memberId -> memberRepository.save(RoomMember(roomId = room.id, memberId = memberId)) }
            .then()
    }

    fun list(): Flux<RoomDTO> {
        return securityManager.getCurrentUserEmail().toMono()
            .flatMap { userViewRepository.findByEmail(it) }
            .flatMapMany { currentUser ->
                memberRepository.findByMemberId(currentUser.id)
                    .flatMap { roomMember ->
                        Mono.zip(
                            roomViewRepository.findById(roomMember.roomId),
                            messageViewRepository.findByRoomIdOrderByDateCreated(roomMember.roomId).collectList()
                        ).flatMap { (roomView, roomMessages) ->
                            if (roomView.createdBy != currentUser.id && roomMessages.isEmpty())
                                Mono.empty()
                            else
                                mapper.convertValue(roomView, RoomDTO::class.java).toMono()
                        }
                    }
            }
    }
}
