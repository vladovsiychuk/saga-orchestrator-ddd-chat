package com.rest_service.saga_orchestrator.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.RoomCommand
import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.EventFactory
import com.rest_service.saga_orchestrator.infrastructure.SagaEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import io.micronaut.context.event.ApplicationEventPublisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

class SagaEventHandlerSpec extends Specification {

    ObjectMapper mapper = new ObjectMapper()

    SagaEventRepository repository = Mock()
    ApplicationEventPublisher<DomainEvent> applicationEventPublisher = Mock()
    SecurityManager securityManager = Mock()
    EventFactory eventFactory = Mock()

    RoomCreateSagaEventHandler handler

    def setup() {
        securityManager.getUserEmail() >> "test-user"
        handler = new RoomCreateSagaEventHandler(repository, applicationEventPublisher, securityManager, eventFactory)
    }

    def "messageActionListener publishes event on successful handling"() {
        given: 'An event and its corresponding saga event and expected domain event for a successful scenario'
        UUID operationId = UUID.randomUUID()
        UUID userId = UUID.randomUUID()
        RoomCommand command = new RoomCommand(userId)
        DomainEvent event = anyCorrectDomainEvent(operationId, command)
        SagaEvent sagaEvent = anyCorrectSagaEvent(operationId, userId)
        DomainEvent expectedDomainEvent = new DomainEvent(EventType.ROOM_CREATE_INITIATE, operationId, ServiceEnum.SAGA_SERVICE, "user@example.com", command)

        1 * repository.save(_) >> Mono.just(sagaEvent)
        1 * repository.findByOperationIdOrderByDateCreated(_) >> Flux.empty()

        when: 'The event handler processes the event'
        handler.messageActionListener(event)

        then: 'The follow-up domain event is published'
        1 * applicationEventPublisher.publishEventAsync(expectedDomainEvent)
    }

    def "messageActionListener publishes rejected event on error when no rejected event saved"() {
        given: 'An event and its corresponding saga event and expected domain event for a successful scenario'
        UUID operationId = UUID.randomUUID()
        UUID userId = UUID.randomUUID()
        RoomCommand command = new RoomCommand(userId)
        DomainEvent event = anyCorrectDomainEvent(operationId, command)
        DomainEvent expectedErrorEvent = new DomainEvent(EventType.ROOM_CREATE_REJECT, operationId, ServiceEnum.SAGA_SERVICE, "test-user", ["message": "exception message"])

        1 * repository.save(_) >> Mono.error(new Exception("exception message"))
        1 * repository.findByOperationIdOrderByDateCreated(_) >> Flux.empty()
        1 * repository.findByOperationIdAndType(_, _) >> Mono.empty()

        when: 'The event handler processes the event and encounters an error'
        handler.messageActionListener(event)

        then: 'A reject event is published'
        1 * applicationEventPublisher.publishEventAsync(expectedErrorEvent)
    }

    def "messageActionListener does nothing on error when rejected event already saved"() {
        given: 'An event and its corresponding saga event for an error scenario with an existing reject event'
        UUID operationId = UUID.randomUUID()
        UUID userId = UUID.randomUUID()
        RoomCommand command = new RoomCommand(userId)
        DomainEvent event = anyCorrectDomainEvent(operationId, command)
        SagaEvent sagaEvent = anyCorrectSagaEvent(operationId, userId)

        1 * repository.save(_) >> Mono.error(new Exception("exception message"))
        1 * repository.findByOperationIdOrderByDateCreated(_) >> Flux.empty()
        1 * repository.findByOperationIdAndType(_, _) >> Mono.just(sagaEvent)

        when: 'The event handler processes the event and encounters an error'
        handler.messageActionListener(event)

        then: 'No new events are published'
        0 * applicationEventPublisher.publishEventAsync(_)
    }

    private SagaEvent anyCorrectSagaEvent(UUID operationId, UUID userId) {
        new SagaEvent(UUID.randomUUID(), operationId, ["userId": userId.toString()], ServiceEnum.SAGA_SERVICE, "test-user", EventType.USER_CREATE_START, 123123)
    }

    private DomainEvent anyCorrectDomainEvent(UUID operationId, RoomCommand command) {
        new DomainEvent(EventType.ROOM_CREATE_START, operationId, ServiceEnum.SAGA_SERVICE, "user@example.com", command)
    }
}
