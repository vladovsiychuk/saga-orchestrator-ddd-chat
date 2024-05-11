package com.saga_orchestrator_ddd_chat.read_service.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.dto.MessageDTO
import com.saga_orchestrator_ddd_chat.read_service.SecurityManager
import com.saga_orchestrator_ddd_chat.read_service.entity.MessageView
import com.saga_orchestrator_ddd_chat.read_service.exception.NotFoundException
import com.saga_orchestrator_ddd_chat.read_service.repository.MessageViewRepository
import com.saga_orchestrator_ddd_chat.read_service.repository.RoomMemberRepository
import com.saga_orchestrator_ddd_chat.read_service.repository.UserViewRepository
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono

@Singleton
class MessageService(
    private val messageViewRepository: MessageViewRepository,
    private val securityManager: SecurityManager,
    private val userViewRepository: UserViewRepository,
    private val roomMemberRepository: RoomMemberRepository,
) {

    val mapper = jacksonObjectMapper()

    fun updateMessage(message: MessageDTO) {
        val messageEntity = mapper.convertValue(message, MessageView::class.java)

        messageViewRepository.findById(messageEntity.id)
            .flatMap { messageViewRepository.update(messageEntity) }
            .switchIfEmpty { messageViewRepository.save(messageEntity) }
            .subscribe()
    }

    fun get(messageId: UUID): Mono<MessageDTO> {
        return messageViewRepository.findById(messageId)
            .map { mapper.convertValue(it, MessageDTO::class.java) }
            .switchIfEmpty(NotFoundException("Message with id $messageId does not exist.").toMono())
    }

    fun getMessagesByRoomId(roomId: UUID): Flux<MessageDTO> {
        return messageViewRepository.findByRoomIdOrderByDateCreated(roomId)
            .map { mapper.convertValue(it, MessageDTO::class.java) }
    }

    fun list(roomLimit: Int): Flux<MessageDTO> {
        return userViewRepository.findByEmail(securityManager.getCurrentUserEmail())
            .flatMapMany { user -> roomMemberRepository.findByMemberId(user.id) }
            .flatMap { room ->
                messageViewRepository.findByRoomIdOrderByDateCreated(room.roomId).takeLast(roomLimit)
            }
            .map { mapper.convertValue(it, MessageDTO::class.java) }
    }
}
