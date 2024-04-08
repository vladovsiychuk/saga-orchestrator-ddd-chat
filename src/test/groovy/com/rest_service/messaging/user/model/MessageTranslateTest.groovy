package com.rest_service.messaging.user.model

import com.rest_service.commons.enums.LanguageEnum
import com.rest_service.commons.enums.SagaEventType
import com.rest_service.commons.enums.UserType
import spock.lang.Specification

import static com.rest_service.Fixture.anyValidMessageTranslateCommand
import static com.rest_service.Fixture.anyValidUserCreateCommand
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.MESSAGE_TRANSLATE_APPROVED
import static com.rest_service.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.rest_service.messaging.user.model.UserDomainDSL.aUser
import static com.rest_service.messaging.user.model.UserDomainDSL.the
import static com.rest_service.messaging.user.model.UserDomainEventDSL.anEvent

class MessageTranslateTest extends Specification {

    def 'should approve message translate event when the user is a translator'() {
        given: 'an existing translator user for English'
        def user = aUser()
        def createTranslatorCommand = anyValidUserCreateCommand()
        createTranslatorCommand['type'] = UserType.TRANSLATOR
        createTranslatorCommand['translationLanguages'] = [LanguageEnum.ENGLISH]
        def createdEvent = anEvent() ofType USER_CREATED withPayload createTranslatorCommand
        the user reactsTo createdEvent

        and: 'request to approve the message translate'
        def translationCommand = anyValidMessageTranslateCommand()
        translationCommand['language'] = LanguageEnum.ENGLISH
        def messageTranslateEvent = anEvent() ofType MESSAGE_TRANSLATE_APPROVED withPayload translationCommand

        when:
        the user reactsTo messageTranslateEvent

        then:
        (the user responseEvent() type) == SagaEventType.MESSAGE_TRANSLATE_APPROVED
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

    def 'should throw an error when the translator user doesnt have the translation language of the message he is trying to translate'() {
        given: 'an existing translator user for English'
        def user = aUser()
        def createTranslatorCommand = anyValidUserCreateCommand()
        createTranslatorCommand['type'] = UserType.TRANSLATOR
        createTranslatorCommand['translationLanguages'] = [LanguageEnum.ENGLISH]
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
        def user = aUser() withResponsibleUserEmail "example@test.com"

        and: 'request to approve the room creation'
        def roomCreatedEvent = anEvent() ofType MESSAGE_TRANSLATE_APPROVED withPayload anyValidMessageTranslateCommand() from "example@test.com"

        when:
        the user reactsTo roomCreatedEvent

        then:
        thrown(UnsupportedOperationException)
    }
}
