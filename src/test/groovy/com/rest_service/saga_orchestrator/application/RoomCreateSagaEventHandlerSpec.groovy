package com.rest_service.saga_orchestrator.application


import com.rest_service.commons.SagaEvent
import com.rest_service.commons.command.RoomCreateCommand
import com.rest_service.commons.dto.ErrorDTO
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.saga_orchestrator.infrastructure.SagaDomainEvent
import com.rest_service.saga_orchestrator.infrastructure.SagaEventRepository
import com.rest_service.saga_orchestrator.infrastructure.SecurityManager
import io.micronaut.context.event.ApplicationEventPublisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

class RoomCreateSagaEventHandlerSpec extends Specification {

    SagaEventRepository repository = Mock()
    ApplicationEventPublisher<SagaEvent> applicationEventPublisher = Mock()
    SecurityManager securityManager = Mock()

    RoomCreateSagaEventHandler handler

    def setup() {
        securityManager.getCurrentUserEmail() >> "example@test.com"
        handler = new RoomCreateSagaEventHandler(repository, applicationEventPublisher, securityManager)
    }

    def "messageActionListener publishes event on successful handling"() {
        given: 'An event for a successful scenario'
        UUID operationId = UUID.randomUUID()
        UUID companionId = UUID.randomUUID()
        String responsibleUserEmail = "example@test.com"
        UUID responsibleUserId = UUID.randomUUID()

        RoomCreateCommand command = new RoomCreateCommand(companionId)
        SagaEvent event = new SagaEvent(SagaEventType.ROOM_CREATE_START, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, command)
        SagaDomainEvent domainEvent = new SagaDomainEvent(UUID.randomUUID(), UUID.randomUUID(), [:], ServiceEnum.SAGA_SERVICE, "foo", null, SagaEventType.ROOM_CREATE_START, 132123)
        SagaEvent expectedSagaEvent = new SagaEvent(SagaEventType.ROOM_CREATE_INITIATED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, command)

        1 * repository.findByOperationIdOrderByDateCreated(_) >> Flux.empty()
        1 * repository.save(_) >> Mono.just(domainEvent)

        when: 'The event handler processes the event'
        handler.handleEvent(event)

        then: 'The follow-up domain event is published'
        1 * applicationEventPublisher.publishEventAsync(expectedSagaEvent)
    }

    def "messageActionListener publishes rejected event on error when no rejected event saved"() {
        given: 'An event and its corresponding saga event and expected domain event for a successful scenario'
        UUID operationId = UUID.randomUUID()
        UUID companionId = UUID.randomUUID()
        String responsibleUserEmail = "example@test.com"
        UUID responsibleUserId = UUID.randomUUID()

        RoomCreateCommand command = new RoomCreateCommand(companionId)
        SagaEvent event = new SagaEvent(SagaEventType.ROOM_CREATE_START, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, command)
        SagaEvent expectedErrorEvent = new SagaEvent(SagaEventType.ROOM_CREATE_REJECTED, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, new ErrorDTO("exception message"))

        1 * repository.save(_) >> Mono.error(new Exception("exception message"))
        1 * repository.findByOperationIdOrderByDateCreated(_) >> Flux.empty()
        1 * repository.existsByOperationIdAndType(_, _) >> Mono.just(false)

        when: 'The event handler processes the event and encounters an error'
        handler.handleEvent(event)

        then: 'A reject event is published'
        1 * applicationEventPublisher.publishEventAsync(expectedErrorEvent)
    }

    def "messageActionListener does nothing on error when rejected event already saved"() {
        given: 'An event and its corresponding saga event for an error scenario with an existing reject event'
        UUID operationId = UUID.randomUUID()
        UUID companionId = UUID.randomUUID()
        String responsibleUserEmail = "example@test.com"
        UUID responsibleUserId = UUID.randomUUID()

        RoomCreateCommand command = new RoomCreateCommand(companionId)
        SagaEvent event = new SagaEvent(SagaEventType.ROOM_CREATE_START, operationId, ServiceEnum.SAGA_SERVICE, responsibleUserEmail, responsibleUserId, command)

        1 * repository.save(_) >> Mono.error(new Exception("exception message"))
        1 * repository.findByOperationIdOrderByDateCreated(_) >> Flux.empty()
        1 * repository.existsByOperationIdAndType(_, _) >> Mono.just(true)

        when: 'The event handler processes the event and encounters an error'
        handler.handleEvent(event)

        then: 'No new events are published'
        0 * applicationEventPublisher.publishEventAsync(_)
    }
}
