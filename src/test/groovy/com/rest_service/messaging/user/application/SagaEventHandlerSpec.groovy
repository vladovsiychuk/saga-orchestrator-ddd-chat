package com.rest_service.messaging.user.application

import com.rest_service.commons.DomainEvent
import com.rest_service.commons.command.UserCommand
import com.rest_service.commons.dto.UserDTO
import com.rest_service.commons.enums.EventType
import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.ServiceEnum
import com.rest_service.commons.enums.UserType
import com.rest_service.messaging.user.infrastructure.UserDomainEvent
import com.rest_service.messaging.user.infrastructure.UserDomainEventRepository
import com.rest_service.messaging.user.model.TimeUtils
import io.micronaut.context.event.ApplicationEventPublisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

class SagaEventHandlerSpec extends Specification {

    UserDomainEventRepository repository = Mock()
    ApplicationEventPublisher<DomainEvent> applicationEventPublisher = Mock()

    SagaEventHandler handler

    def setup() {
        handler = new SagaEventHandler(repository, applicationEventPublisher)
    }

    def "messageActionListener publishes event on successful handling"() {
        given: 'An event and its corresponding user domain event and expected domain event for a successful scenario'
        TimeUtils.@Companion.setFixedClock(1000000000000)
        UUID operationId = UUID.randomUUID()
        UUID userId = UUID.randomUUID()
        UUID temporaryId = UUID.randomUUID()
        UserCommand command = new UserCommand(userId, UserType.REGULAR_USER, null, "user@example.com", LanguageEnum.ENGLISH, null, temporaryId)
        UserDTO userDto = new UserDTO(userId, temporaryId, null, "user@example.com", null, LanguageEnum.ENGLISH, null, UserType.REGULAR_USER, 1000000000000, 1000000000000)

        DomainEvent event = anyCorrectDomainEvent(operationId, command)
        UserDomainEvent userDomainEvent = anyCorrectUserDomainEvent(operationId, userId, temporaryId)
        DomainEvent expectedDomainEvent = new DomainEvent(EventType.USER_CREATE_APPROVE, operationId, ServiceEnum.USER_SERVICE, "user@example.com", userDto)

        1 * repository.save(_) >> Mono.just(userDomainEvent)
        2 * repository.findByEmailOrderByDateCreated(_) >> Flux.empty()

        when: 'The event handler processes the event'
        handler.messageActionListener(event)

        then: 'The follow-up domain event is published'
        1 * applicationEventPublisher.publishEventAsync(expectedDomainEvent)
    }

    private UserDomainEvent anyCorrectUserDomainEvent(UUID operationId, UUID userId, UUID temporaryId) {
        new UserDomainEvent(UUID.randomUUID(), userId, "user@example.com", [userId: userId, type: UserType.REGULAR_USER, email: "user@example.com", primaryLanguage: LanguageEnum.ENGLISH, temporaryId: temporaryId], EventType.USER_CREATE_INITIATE, operationId, 123123)
    }

    private DomainEvent anyCorrectDomainEvent(UUID operationId, UserCommand command) {
        new DomainEvent(EventType.USER_CREATE_INITIATE, operationId, ServiceEnum.SAGA_SERVICE, "user@example.com", command)
    }
}
