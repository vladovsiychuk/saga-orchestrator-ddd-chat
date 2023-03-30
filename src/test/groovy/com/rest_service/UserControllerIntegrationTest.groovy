package com.rest_service

import com.rest_service.domain.User
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
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions

@MicronautTest(transactional = false)
@Requires(env = Environment.TEST)
class UserControllerIntegrationTest extends Specification {

    @Inject
    @Client("/v1/users")
    ReactorHttpClient client

    @Inject
    @Shared
    UserRepository repository

    @Shared
    String regular_user_1
    @Shared
    String user_2
    @Shared
    String user_3
    @Shared
    String translator_user_4

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    def setupSpec() {
        regular_user_1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.B7NnRHclfkrcOgK4HX8fqogY-oq3Hv1GrWTylDqOhrg"
        user_2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTJAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.2g43we2jYBkY15SmDvV4fz_bfgGvltY5F5udXUKRi2c"
        user_3 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTNAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ._7KPCfylisPdIyvXcKAEjGKUPyKV_hfXKDsm4faF_4U"
        translator_user_4 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTRAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.lRP3YeXmsEIw-xDwgaHgzyBB1Z3xowApEvq4W4VWsx8"

        def regularUser = new User(UUID.fromString("50b1cccd-2cbd-459b-9b63-c97145b97e94"), null, "user-1@gmail.com", null, LanguageEnum.ENGLISH, null, UserType.REGULAR_USER, 1, 1)
        def translatorUser = new User(UUID.fromString("af7a16ae-cc5d-484e-8b1d-e89fd52d1150"), null, "user-4@gmail.com", null, LanguageEnum.ENGLISH, [LanguageEnum.ENGLISH.toString(), LanguageEnum.UKRAINIAN.toString()], UserType.TRANSLATOR, 1, 1)

        repository.save(regularUser).block()
        repository.save(translatorUser).block()
    }

    @Unroll
    void "GET should return the DTO of current #expectedType user"() {
        when:
        def request = HttpRequest.GET("/currentUser").bearerAuth(token)
        def response = client.toBlocking().exchange(request, Object)

        then:
        response.body() == [
            id                  : expectedUserId,
            username            : null,
            email               : expectedEmail,
            avatar              : null,
            primaryLanguage     : LanguageEnum.ENGLISH.toString(),
            translationLanguages: expectedTranslationLanguages,
            type                : expectedType,
            dateCreated         : 1,
            dateUpdated         : 1,
        ]

        where:
        expectedType                     | expectedTranslationLanguages                                         | expectedUserId                         | expectedEmail      | token
        UserType.REGULAR_USER.toString() | null                                                                 | "50b1cccd-2cbd-459b-9b63-c97145b97e94" | "user-1@gmail.com" | regular_user_1
        UserType.TRANSLATOR.toString()   | [LanguageEnum.ENGLISH.toString(), LanguageEnum.UKRAINIAN.toString()] | "af7a16ae-cc5d-484e-8b1d-e89fd52d1150" | "user-4@gmail.com" | translator_user_4
    }

    void "GET should return 404 response if the user doesn't exist"() {
        when:
        def request = HttpRequest.GET("/currentUser").bearerAuth(user_2)
        client.toBlocking().exchange(request, Object)

        then:
        HttpClientResponseException ex = thrown()
        ex.response.code() == HttpStatus.NOT_FOUND.code
    }

    @Unroll
    void "POST should create a new #type user from authorization"() {
        when:
        def request = HttpRequest.POST("/currentUser", command).bearerAuth(user_3)
        def response = client.toBlocking().exchange(request, Map)

        then:
        def body = response.body()
        body.username == null
        body.email == "user-3@gmail.com"
        body.avatar == null
        body.primaryLanguage == LanguageEnum.ENGLISH.toString()
        body.translationLanguages?.sort() == command.translationLanguages?.sort()
        body.type == type

        cleanup:
        repository.deleteAll().block()

        where:
        type                             | command
        UserType.REGULAR_USER.toString() | [type: type, primaryLanguage: LanguageEnum.ENGLISH.toString()]
        UserType.TRANSLATOR.toString()   | [type: type, primaryLanguage: LanguageEnum.ENGLISH.toString(), translationLanguages: [LanguageEnum.ENGLISH.toString(), LanguageEnum.UKRAINIAN.toString()]]
    }

    @Unroll
    void "POST should IllegalArgument exception if #reason"() {
        when:
        def request = HttpRequest.POST("/currentUser", command).bearerAuth(user_3)
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

    void "only cleanup"() {
        cleanup:
        repository.deleteAll().block()
    }
}
