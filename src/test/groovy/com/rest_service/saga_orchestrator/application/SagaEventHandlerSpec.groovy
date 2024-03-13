package com.rest_service.saga_orchestrator.application

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.RoomCommand
import com.rest_service.commons.enums.SagaType
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

    SagaEventRepository repository = Mock()
    ApplicationEventPublisher<DomainEvent> applicationEventPublisher = Mock()
    SecurityManager securityManager = Mock()
    EventFactory eventFactory = Mock()

    RoomCreateSagaEventHandler handler

    def setup() {
        handler = new RoomCreateSagaEventHandler(repository, applicationEventPublisher, securityManager, eventFactory)
    }

    def "messageActionListener publishes event on successful handling"() {
        given:
        UUID operationId = UUID.randomUUID()
        UUID userId = UUID.randomUUID()
        RoomCommand command = new RoomCommand(userId)
        DomainEvent event = new DomainEvent(SagaType.ROOM_CREATE_START, operationId, ServiceEnum.SAGA_SERVICE, "user@example.com", command)
        SagaEvent sagaEvent = new SagaEvent(UUID.randomUUID(), operationId, ["userId": userId.toString()], ServiceEnum.SAGA_SERVICE, "test-user", SagaType.USER_CREATE_START, 123123)
        DomainEvent expectedDomainEvent = new DomainEvent(SagaType.ROOM_CREATE_INITIATE, operationId, ServiceEnum.SAGA_SERVICE, "test-user", command)

        1 * repository.save(_) >> Mono.just(sagaEvent)
        1 * repository.findByOperationIdOrderByDateCreated(_) >> Flux.just(sagaEvent)

        when:
        handler.messageActionListener(event)

        then:
        1 * applicationEventPublisher.publishEventAsync(expectedDomainEvent)
    }
}
