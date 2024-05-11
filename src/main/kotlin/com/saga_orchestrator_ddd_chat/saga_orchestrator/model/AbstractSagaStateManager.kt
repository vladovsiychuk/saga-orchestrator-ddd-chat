package com.saga_orchestrator_ddd_chat.saga_orchestrator.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.saga_orchestrator_ddd_chat.commons.Domain
import com.saga_orchestrator_ddd_chat.commons.SagaEvent
import com.saga_orchestrator_ddd_chat.commons.command.Command
import com.saga_orchestrator_ddd_chat.commons.dto.DTO
import com.saga_orchestrator_ddd_chat.commons.dto.ErrorDTO
import com.saga_orchestrator_ddd_chat.commons.enums.SagaEventType
import com.saga_orchestrator_ddd_chat.commons.enums.ServiceEnum
import com.saga_orchestrator_ddd_chat.saga_orchestrator.infrastructure.SagaDomainEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

abstract class AbstractSagaStateManager<C : Command, D : DTO> : Domain {
    var state: SagaState = ReadyState()
    val approvedServices: MutableList<ServiceEnum> = mutableListOf()
    lateinit var command: C
    lateinit var dto: D
    var errorDto: ErrorDTO? = null

    abstract fun startEvent(): SagaEventType
    abstract fun approveEvent(): SagaEventType
    abstract fun rejectEvent(): SagaEventType
    abstract fun transformCommand(payload: Map<String, Any>): C
    abstract fun transformDTO(payload: Map<String, Any>): D
    abstract fun isComplete(): Boolean
    abstract fun mainDomainService(): ServiceEnum
    abstract fun createInitiatedResponseEvent(): SagaEvent
    abstract fun createCompletedResponseEvent(): SagaEvent
    abstract fun createErrorResponseEvent(): SagaEvent

    private val mapper = jacksonObjectMapper()
    fun apply(event: SagaDomainEvent) = state.apply(event)
    fun createResponseSagaEvent() = state.createSagaResponseEvent()

    inner class ReadyState : SagaState {
        override fun apply(event: SagaDomainEvent): SagaDomainEvent {
            return when (event.type) {
                startEvent()  -> {
                    command = transformCommand(event.payload)
                    state = InitiatedState()
                    event
                }

                rejectEvent() -> {
                    errorDto = mapper.convertValue(event.payload, ErrorDTO::class.java)
                    state = ErrorState()
                    event
                }

                else          -> throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
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
                    state = if (isComplete()) CompletedState() else InApprovingState()
                    event
                }

                rejectEvent()  -> {
                    errorDto = mapper.convertValue(event.payload, ErrorDTO::class.java)
                    state = ErrorState()
                    event
                }

                else           -> throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
            }
        }

        override fun createSagaResponseEvent() = createInitiatedResponseEvent().toMono()
    }

    inner class InApprovingState : SagaState {
        override fun apply(event: SagaDomainEvent): SagaDomainEvent {
            state = InitiatedState()
            return state.apply(event)
        }

        override fun createSagaResponseEvent(): Mono<SagaEvent> = Mono.empty()
    }

    inner class ErrorState : SagaState {
        override fun apply(event: SagaDomainEvent) = throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        override fun createSagaResponseEvent() = createErrorResponseEvent().toMono()
    }

    inner class CompletedState : SagaState {
        override fun apply(event: SagaDomainEvent) = throw UnsupportedOperationException("Operation with type ${event.type} is not supported.")
        override fun createSagaResponseEvent() = createCompletedResponseEvent().toMono()
    }

    interface SagaState {
        fun apply(event: SagaDomainEvent): SagaDomainEvent
        fun createSagaResponseEvent(): Mono<SagaEvent>
    }
}
