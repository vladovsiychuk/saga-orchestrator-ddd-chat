package com.saga_orchestrator_ddd_chat.messaging.user.model

import com.saga_orchestrator_ddd_chat.commons.enums.LanguageEnum
import com.saga_orchestrator_ddd_chat.commons.enums.UserType
import spock.lang.Specification
import spock.lang.Unroll

import static UserDSL.aUser
import static UserDSL.the
import static com.saga_orchestrator_ddd_chat.Fixture.anyValidUserCreateCommand
import static com.saga_orchestrator_ddd_chat.messaging.user.infrastructure.UserDomainEventType.USER_CREATED
import static com.saga_orchestrator_ddd_chat.messaging.user.model.UserDomainEventDSL.anEvent

class UserCreateTest extends Specification {

    def 'should approve the creation of regular user on successful event'() {
        given: 'a regular user in creation'
        def user = aUser()

        and: 'a user create event'
        def inputEvent = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()

        when:
        the user reactsTo inputEvent

        then:
        (the user data()) != null
    }

    def 'should approve the creation of translator user on successful event'() {
        given: 'a regular user in creation'
        def user = aUser()

        and: 'a translator user create event'
        def command = anyValidUserCreateCommand()
        command['primaryLanguage'] = LanguageEnum.ENGLISH
        command['type'] = UserType.TRANSLATOR
        command['translationLanguages'] = [LanguageEnum.UKRAINIAN]
        def inputEvent = anEvent() ofType USER_CREATED withPayload command

        when:
        the user reactsTo inputEvent

        then:
        (the user data()).translationLanguages.sort() == [LanguageEnum.ENGLISH, LanguageEnum.UKRAINIAN].sort()
    }

    @Unroll
    def 'should throw an error when trying to create a translator user and #reason'() {
        given: 'a user in creation'
        def user = aUser()

        and: 'a command with missing translationLanguages'
        def command = anyValidUserCreateCommand()
        command['primaryLanguage'] = LanguageEnum.ENGLISH
        command['type'] = UserType.TRANSLATOR
        command['translationLanguages'] = translationLanguages
        def inputEvent = anEvent() ofType USER_CREATED withPayload command

        when:
        the user reactsTo inputEvent

        then:
        thrown(RuntimeException)

        where:
        reason                                                               | translationLanguages
        'translationLanguages property is missing from command'              | null
        'translationLanguages does contain same language as primaryLanguage' | [LanguageEnum.ENGLISH]
    }

    def 'should throw an error when trying to create a duplicate user'() {
        given: 'a user domain that has already processed a user creation event'
        def event = anEvent() ofType USER_CREATED withPayload anyValidUserCreateCommand()
        def user = aUser() reactsTo event

        when: 'the same user creation event is processed again'
        the user reactsTo event

        then: 'an error is thrown indicating the user is already created'
        thrown(RuntimeException)
    }

    def 'should throw an error when the request is not for the current user'() {
        given: 'a regular user in creation'
        def user = aUser()

        and: 'a command for another user'
        def command = anyValidUserCreateCommand()
        command['email'] = 'any-other@email.com'

        and: 'an event for different user'
        def inputEvent = anEvent() ofType USER_CREATED withPayload command

        when:
        the user reactsTo inputEvent

        then:
        thrown(RuntimeException)
    }
}
