package com.rest_service.saga_orchestrator.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.State
import com.rest_service.commons.command.Command
import com.rest_service.commons.dto.DTO
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

abstract class AbstractSagaState : State {
    lateinit var currentUserEmail: String
    val approvedServices: MutableList<ServiceEnum> = mutableListOf()
    var status: SagaStatus = SagaStatus.READY
    lateinit var data: DTO
    lateinit var command: Command
    var errorDto: ErrorDTO? = null

    private val mapper = jacksonObjectMapper()

    abstract fun initiateSaga(event: SagaEvent): Mono<Boolean>
    abstract fun approveSaga(event: SagaEvent): Mono<Boolean>
    abstract fun rejectSaga(event: SagaEvent): Mono<Boolean>
    abstract fun isComplete(): Boolean
    abstract fun createInitiateEvent(): Mono<DomainEvent>
    abstract fun createCompleteEvent(): Mono<DomainEvent>
    abstract fun createErrorEvent(): Mono<DomainEvent>

    override fun apply(event: DomainEvent): Mono<Boolean> {
        val sagaEvent = convertEventData(event, SagaEvent::class.java)
        return apply(sagaEvent)
    }

    fun apply(sagaEvent: SagaEvent): Mono<Boolean> {
        return when {
            sagaEvent.type.name.endsWith("_START")   -> initiateSaga(sagaEvent)
            sagaEvent.type.name.endsWith("_APPROVE") -> approveSaga(sagaEvent)
            sagaEvent.type.name.endsWith("_REJECT")  -> rejectSaga(sagaEvent)
            else                                     -> {
                true.toMono()
            }
        }
    }

    override fun createNextEvent(): Mono<DomainEvent> = when (status) {
        SagaStatus.INITIATED    -> createInitiateEvent()
        SagaStatus.COMPLETED    -> createCompleteEvent()
        SagaStatus.REJECTED     -> createErrorEvent()
        SagaStatus.IN_APPROVING -> Mono.empty()
        else                    -> Mono.error(UnsupportedOperationException())
    }

    fun transitionTo(nextStatus: SagaStatus): Mono<Boolean> {
        validateStatusTransition(nextStatus)
        status = nextStatus
        return true.toMono()
    }

    private fun validateStatusTransition(wantedStatus: SagaStatus) {
        val validPreviousStates = mapOf(
            SagaStatus.INITIATED to setOf(SagaStatus.READY),
            SagaStatus.IN_APPROVING to setOf(SagaStatus.INITIATED),
            SagaStatus.COMPLETED to setOf(SagaStatus.INITIATED, SagaStatus.IN_APPROVING),
            SagaStatus.REJECTED to setOf(SagaStatus.INITIATED),
        )

        if (status !in validPreviousStates[wantedStatus].orEmpty())
            throw RuntimeException("Status cannot be changed from $status to $wantedStatus")
    }

    protected fun <V> convertEventData(payload: Any, clazz: Class<V>): V =
        mapper.convertValue(payload, clazz) ?: throw RuntimeException("Error converting event data")
}
