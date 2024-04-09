package com.rest_service.messaging.message.model

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.SagaEventType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidMessageCreateCommand
import static com.rest_service.Fixture.anyValidMessageTranslateCommand
import static com.rest_service.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_CREATED
import static com.rest_service.messaging.message.infrastructure.MessageDomainEventType.MESSAGE_TRANSLATED
import static com.rest_service.messaging.message.model.MessageDomainDSL.aMessage
import static com.rest_service.messaging.message.model.MessageDomainDSL.the
import static com.rest_service.messaging.message.model.MessageDomainEventDSL.anEvent

class MessageTranslateTest extends Specification {

    def 'should approve message translate when the translation does not exists yet'() {
        given: 'a created message'
        def message = aMessage()
        def messageCreatedEvent = anEvent() ofType MESSAGE_CREATED withPayload anyValidMessageCreateCommand()
        the message reactsTo messageCreatedEvent

        and: 'a message translate event'
        def translateCommand = anyValidMessageTranslateCommand()
        translateCommand['language'] = 'ENGLISH'
        translateCommand['translation'] = 'new translation text'
        def messageTranslateEvent = anEvent() ofType MESSAGE_TRANSLATED withPayload translateCommand

        when:
        the message reactsTo messageTranslateEvent

        then:
        (the message responseEvent() type) == SagaEventType.MESSAGE_TRANSLATE_APPROVED

        and:
        message.domain.message.translations.find { it.language == LanguageEnum.ENGLISH }.translation == 'new translation text'
    }

    def 'should throw an error when trying to translate the message to already translated language'() {
        given: 'a created message'
        def message = aMessage()
        def messageCreatedEvent = anEvent() ofType MESSAGE_CREATED withPayload anyValidMessageCreateCommand()
        the message reactsTo messageCreatedEvent

        and: 'existing translation'
        def translateCommand = anyValidMessageTranslateCommand()
        translateCommand['language'] = 'ENGLISH'
        translateCommand['translation'] = 'new translation text'
        def messageTranslateEvent = anEvent() ofType MESSAGE_TRANSLATED withPayload translateCommand
        the message reactsTo messageTranslateEvent

        when: 'message react to the same translation'
        the message reactsTo messageTranslateEvent

        then:
        thrown(RuntimeException)
    }
}
