package com.saga_orchestrator_ddd_chat.messaging.user.model

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import com.saga_orchestrator_ddd_chat.commons.enums.UserType
import spock.lang.Specification

import static UserDSL.aUser
import static UserDSL.the
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidMessageTranslateCommand
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidUserCreateCommand
import static com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventType.MESSAGE_TRANSLATE_APPROVED
import static com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.saga_orchestrator_ddd_chat.messaging.user.model.UserDomainEventDSL.anEvent

class MessageTranslateTest extends Specification {

    def 'should approve message translation when the user is a translator and can translate the language'() {
        given: 'an existing translator user for English'
        def user = aUser()
        def createTranslatorCommand = anyValidUserCreateCommand()
        createTranslatorCommand['type'] = UserType.TRANSLATOR
        createTranslatorCommand['translationLanguages'] = [LanguageEnum.UKRAINIAN]
        def createdEvent = anEvent() ofType USER_CREATED withPayload createTranslatorCommand
        the user reactsTo createdEvent

        and: 'a request to approve the translation'
        def translationCommand = anyValidMessageTranslateCommand()
        translationCommand['language'] = LanguageEnum.ENGLISH
        def messageTranslateEvent = anEvent() ofType MESSAGE_TRANSLATE_APPROVED withPayload translationCommand

        when:
        the user reactsTo messageTranslateEvent

        then:
        (the user data()) != null
    }

    def 'should throw an error when a regular user is trying to translate the message'() {
        given: 'an existing regular user'
        def user = aUser()
        def createTranslatorCommand = anyValidUserCreateCommand()
        createTranslatorCommand['type'] = UserType.REGULAR_USER
        def createdEvent = anEvent() ofType USER_CREATED withPayload createTranslatorCommand
        the user reactsTo createdEvent

        and: 'request to approve the message translate'
        def messageTranslateEvent = anEvent() ofType MESSAGE_TRANSLATE_APPROVED withPayload anyValidMessageTranslateCommand()

        when:
        the user reactsTo messageTranslateEvent

        then:
        thrown(RuntimeException)
    }

    def 'should throw an error when the translator user doesnt have the translation language for the message he is trying to translate'() {
        given: 'an existing translator user for English'
        def user = aUser()
        def createTranslatorCommand = anyValidUserCreateCommand()
        createTranslatorCommand['type'] = UserType.TRANSLATOR
        createTranslatorCommand['translationLanguages'] = [LanguageEnum.ITALIAN]
        def createdEvent = anEvent() ofType USER_CREATED withPayload createTranslatorCommand
        the user reactsTo createdEvent

        and: 'request to approve the message translate'
        def translationCommand = anyValidMessageTranslateCommand()
        translationCommand['language'] = LanguageEnum.UKRAINIAN
        def messageTranslateEvent = anEvent() ofType MESSAGE_TRANSLATE_APPROVED withPayload translationCommand

        when:
        the user reactsTo messageTranslateEvent

        then:
        thrown(RuntimeException)
    }

    def 'should throw an error when trying to approve the message translate for not yet created user'() {
        given: 'a user in InCreation state'
        def user = aUser()

        and: 'request to approve the room creation'
        def roomCreatedEvent = anEvent() ofType MESSAGE_TRANSLATE_APPROVED withPayload anyValidMessageTranslateCommand()

        when:
        the user reactsTo roomCreatedEvent

        then:
        thrown(RuntimeException)
    }
}
