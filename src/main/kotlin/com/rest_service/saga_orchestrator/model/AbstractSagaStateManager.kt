package com.rest_service.saga_orchestrator.model

import com.rest_service.commons.Domain
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.Command
import com.rest_service.commons.dto.DTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

abstract class AbstractSagaStateManager<C : Command, D : DTO> : Domain {
    var state: SagaState = ReadyState()
    val approvedServices: MutableList<ServiceEnum> = mutableListOf()
    lateinit var command: C
    lateinit var dto: D

    abstract fun startEvent(): SagaEventType
    abstract fun approveEvent(): SagaEventType
    abstract fun transformCommand(payload: Map<String, Any>): C
    abstract fun transformDTO(payload: Map<String, Any>): D
    abstract fun isComplete(): Boolean
    abstract fun mainDomainService(): ServiceEnum
    abstract fun createInitiatedResponseEvent(): SagaEvent
    abstract fun createCompletedResponseEvent(): SagaEvent

    inner class ReadyState : SagaState {
        override fun apply(event: SagaDomainEvent): SagaDomainEvent {
            return when (event.type) {
                startEvent() -> {
                    command = transformCommand(event.payload)
                    state = InitiatedState()
                    event
                }

                else         -> throw UnsupportedOperationException()
            }
        }

        override fun createSagaResponseEvent() = throw UnsupportedOperationException()
    }

    inner class InitiatedState : SagaState {
        override fun apply(event: SagaDomainEvent): SagaDomainEvent {
            return when (event.type) {
                approveEvent() -> {
                    approvedServices.add(event.responsibleService)
                    if (event.responsibleService == mainDomainService()) dto = transformDTO(event.payload)
                    if (isComplete()) state = CompletedState()
                    event
                }

                else           -> throw UnsupportedOperationException()
            }
        }

        override fun createSagaResponseEvent() = createInitiatedResponseEvent().toMono()
    }

    inner class CompletedState : SagaState {
        override fun apply(event: SagaDomainEvent) = throw UnsupportedOperationException()
        override fun createSagaResponseEvent() = createCompletedResponseEvent().toMono()
    }

    interface SagaState {
        fun apply(event: SagaDomainEvent): SagaDomainEvent
        fun createSagaResponseEvent(): Mono<SagaEvent>
    }
}
