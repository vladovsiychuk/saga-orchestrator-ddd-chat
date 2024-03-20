package com.rest_service.messaging.user.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.State
import com.rest_service.commons.command.UserCommand
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.DomainStatus
import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class UserDomain(val currentUserEmail: String, val operationId: UUID) : State {
    private var status = DomainStatus.IN_CREATION
    lateinit var responsibleUserEmail: String
    var responsibleUser: UserDTO? = null
    var currentUser: UserDTO? = null

    private val mapper = jacksonObjectMapper()
    override fun apply(event: DomainEvent): Mono<Boolean> {
        return apply(event.convertEvent())
    }

    fun apply(event: UserDomainEvent): Mono<Boolean> {
        return when (event.type) {
            EventType.USER_CREATE_INITIATE -> createUser(event)
            EventType.USER_CREATE_REJECT   -> rejectUserCreate(event)
            else                           -> {
                true.toMono()
            }
        }
    }

    private fun createUser(event: UserDomainEvent): Mono<Boolean> {
        return when {
            status != DomainStatus.IN_CREATION       -> RuntimeException("User $currentUserEmail is already created.").toMono()
            status == DomainStatus.REJECTED          -> RuntimeException("User $currentUserEmail is in error state.").toMono()
            responsibleUserEmail != currentUserEmail -> RuntimeException("Responsible user doesn't have permissions to create the user").toMono()
            else                                     -> {
                val command = mapper.convertValue(event.payload, UserCommand::class.java)
                currentUser = UserDTO(command, event.dateCreated)
                status = DomainStatus.CREATED
                true.toMono()
            }
        }
    }

    private fun rejectUserCreate(event: UserDomainEvent): Mono<Boolean> {
        if (event.operationId == operationId)
            status = DomainStatus.REJECTED
        return true.toMono()
    }

    override fun createNextEvent(): Mono<DomainEvent> {
        return when (status) {
            DomainStatus.CREATED  -> createApproveEvent()
            DomainStatus.REJECTED -> RuntimeException("User $currentUserEmail is in error state.").toMono()
            else                  -> UnsupportedOperationException().toMono()
        }
    }

    private fun createApproveEvent() = DomainEvent(EventType.USER_CREATE_APPROVE, operationId, ServiceEnum.USER_SERVICE, responsibleUserEmail, currentUser!!).toMono()
}
