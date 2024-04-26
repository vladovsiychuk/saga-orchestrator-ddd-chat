package com.rest_service.messaging.message.application

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rest_service.commons.AbstractEventHandler
import com.rest_service.commons.Domain
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.MessageReadCommand
import com.rest_service.commons.command.MessageTranslateCommand
import com.rest_service.commons.command.MessageUpdateCommand
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.messaging.message.infrastructure.MessageDomainEvent
import com.rest_service.messaging.message.infrastructure.MessageDomainEventType
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.scheduling.annotation.Async
import jakarta.inject.Singleton
import java.util.UUID
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Singleton
open class MessageSagaEventHandler(
    private val rejectEventHandler: RejectEventHandler,
    private val applicationEventPublisher: ApplicationEventPublisher<SagaEvent>,
    private val messageStateManager: MessageStateManager,
) {
    private val mapper = jacksonObjectMapper()

    @EventListener
    @Async
    open fun messageActionListener(event: SagaEvent) {
        when (event.type) {
            SagaEventType.MESSAGE_CREATE_INITIATED    -> handleEventWithMapper(event) {
                messageStateManager.mapDomainEvent(UUID.randomUUID(), MessageDomainEventType.MESSAGE_CREATED, event)
            }

            SagaEventType.MESSAGE_UPDATE_INITIATED    -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, MessageUpdateCommand::class.java)
                messageStateManager.mapDomainEvent(command.messageId, MessageDomainEventType.MESSAGE_UPDATED, event)
            }

            SagaEventType.MESSAGE_READ_INITIATED      -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, MessageReadCommand::class.java)
                messageStateManager.mapDomainEvent(command.messageId, MessageDomainEventType.MESSAGE_READ, event)
            }

            SagaEventType.MESSAGE_TRANSLATE_INITIATED -> handleEventWithMapper(event) {
                val command = mapper.convertValue(event.payload, MessageTranslateCommand::class.java)
                messageStateManager.mapDomainEvent(command.messageId, MessageDomainEventType.MESSAGE_TRANSLATED, event)
            }

            SagaEventType.MESSAGE_CREATE_REJECTED,
            SagaEventType.MESSAGE_UPDATE_REJECTED,
            SagaEventType.MESSAGE_READ_REJECTED,
            SagaEventType.MESSAGE_TRANSLATE_REJECTED  -> rejectEventHandler.handleEvent(event)

            else                                      -> {}
        }
    }

    private fun handleEventWithMapper(sagaEvent: SagaEvent, mapper: () -> DomainEvent) {
        object : AbstractEventHandler(applicationEventPublisher) {
            override fun rebuildDomainFromEvent(domainEvent: DomainEvent): Mono<Domain> {
                domainEvent as MessageDomainEvent
                return messageStateManager.rebuildMessage(domainEvent.messageId, domainEvent.operationId).map { it }
            }

            override fun mapDomainEvent() = mapper().toMono()

            override fun saveEvent(domainEvent: DomainEvent): Mono<DomainEvent> =
                messageStateManager.saveEvent(domainEvent).map { it }

            override fun handleError(error: Throwable) = messageStateManager.handleError(sagaEvent, error)

            override fun createResponseSagaEvent(domain: Domain) =
                SagaEvent(sagaEvent.type.approvedEventType!!, sagaEvent.operationId, ServiceEnum.MESSAGE_SERVICE, sagaEvent.responsibleUserId, domain.toDto()).toMono()

        }.handleEvent()
    }
}
