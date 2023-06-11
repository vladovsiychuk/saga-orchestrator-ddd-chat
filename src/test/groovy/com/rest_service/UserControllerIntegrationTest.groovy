package com.rest_service

import com.rest_service.constant.UserConstant
import com.rest_service.enums.LanguageEnum
import com.rest_service.enums.UserType
import com.rest_service.repository.UserRepository
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.lang.Unroll

@MicronautTest(transactional = false)
@Requires(env = Environment.TEST)
class UserControllerIntegrationTest extends Specification {

    @Inject
    @Client("/v1/users")
    ReactorHttpClient client

    @Inject
    UserRepository repository

    @Unroll
    void "GET should return the DTO of current #expectedType user"() {
        when:
        def request = HttpRequest.GET("/currentUser").bearerAuth(token)
        def response = client.toBlocking().exchange(request, Object)

        then:
        response.body() == [
            id                  : expectedUserId,
            username            : expectedUsername,
            email               : expectedEmail,
            avatar              : expectedAvatar,
            primaryLanguage     : expectedPrimaryLanguage.toString(),
            translationLanguages: expectedTranslationLanguages,
            type                : expectedType,
            dateCreated         : 1,
            dateUpdated         : 1,
        ]

        where:
        expectedType                     | expectedTranslationLanguages                                         | expectedPrimaryLanguage | expectedUserId         | expectedEmail      | expectedUsername | expectedAvatar | token
        UserType.REGULAR_USER.toString() | null                                                                 | LanguageEnum.UKRAINIAN  | UserConstant.USER_1_ID | "user-1@gmail.com" | 'username-1'     | 'avatar-1'     | UserConstant.USER_1_TOKEN
        UserType.TRANSLATOR.toString()   | [LanguageEnum.ENGLISH.toString(), LanguageEnum.UKRAINIAN.toString()] | LanguageEnum.ENGLISH    | UserConstant.USER_4_ID | "user-4@gmail.com" | 'username-4'     | 'avatar-4'     | UserConstant.USER_4_TOKEN
    }

    @Unroll
    void "GET should return list of users when command is #command"() {
        when:
        def request = HttpRequest.GET("/?$command").bearerAuth(UserConstant.USER_1_TOKEN)
        def response = client.toBlocking().exchange(request, List)

        then:
        response.body().collect { it.email }.sort() == expectedUsers.sort()

        where:
        command                      | expectedUsers
        "query=user"                 | ["user-1@gmail.com", "user-2@gmail.com", "user-3@gmail.com", "user-4@gmail.com", "user-5@gmail.com", "user-6@gmail.com"]
        "roomLimit=30"               | ["user-3@gmail.com", "user-4@gmail.com", "user-5@gmail.com", "user-6@gmail.com"]
        "query=user&type=TRANSLATOR" | ["user-4@gmail.com"]
    }

    void "GET should return 404 response if the user doesn't exist"() {
        when:
        def request = HttpRequest.GET("/currentUser").bearerAuth(UserConstant.NOT_EXISTING_USER_TOKEN)
        client.toBlocking().exchange(request, Object)

        then:
        HttpClientResponseException ex = thrown()
        ex.response.code() == HttpStatus.NOT_FOUND.code
    }

    @Unroll
    void "POST should create a new #type user from authorization"() {
        when:
        def request = HttpRequest.POST("/currentUser", command).bearerAuth(UserConstant.NEW_USER_TOKEN)
        def response = client.toBlocking().exchange(request, Map)

        then:
        def body = response.body()
        body.username == null
        body.email == "new-user@gmail.com"
        body.avatar == null
        body.primaryLanguage == LanguageEnum.ENGLISH.toString()
        body.translationLanguages?.sort() == command.translationLanguages?.sort()
        body.type == type

        cleanup:
        repository.deleteById(UUID.fromString(response.body().id.toString())).block()

        where:
        type                             | command
        UserType.REGULAR_USER.toString() | [type: type, primaryLanguage: LanguageEnum.ENGLISH.toString()]
        UserType.TRANSLATOR.toString()   | [type: type, primaryLanguage: LanguageEnum.ENGLISH.toString(), translationLanguages: [LanguageEnum.ENGLISH.toString(), LanguageEnum.UKRAINIAN.toString()]]
    }

    @Unroll
    void "POST should IllegalArgument exception if #reason"() {
        when:
        def request = HttpRequest.POST("/currentUser", command).bearerAuth(UserConstant.NEW_USER_TOKEN)
        client.toBlocking().exchange(request, Map)

        then:
        HttpClientResponseException exception = thrown()
        exception.response.code() == HttpStatus.BAD_REQUEST.code
        exception.message == expectedMessage


        where:
        reason                                     | command                                                                                                                                               | expectedMessage
        "translator has less than 2 languages"     | [type: UserType.TRANSLATOR.toString(), primaryLanguage: LanguageEnum.ENGLISH.toString()]                                                              | "A translator user must have at least 1 translation language."
        "a regular user has translation languages" | [type: UserType.REGULAR_USER.toString(), primaryLanguage: LanguageEnum.ENGLISH.toString(), translationLanguages: [LanguageEnum.UKRAINIAN.toString()]] | "A regular user cannot have translation languages."
    }
}
