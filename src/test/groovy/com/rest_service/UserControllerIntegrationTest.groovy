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
    String tokenUser1

    @Shared
    String tokenUser2

    @Shared
    String tokenUser3

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    def setupSpec() {
        // email : user-1@gmail.com
        tokenUser1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.B7NnRHclfkrcOgK4HX8fqogY-oq3Hv1GrWTylDqOhrg"

        // user doesn't exist in the db
        tokenUser2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTJAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.2g43we2jYBkY15SmDvV4fz_bfgGvltY5F5udXUKRi2c"

        // token for new user with email user-3@gmail.com
        tokenUser3 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTJAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.2g43we2jYBkY15SmDvV4fz_bfgGvltY5F5udXUKRi2c"

        def user1 = new User(UUID.fromString("50b1cccd-2cbd-459b-9b63-c97145b97e94"), null, "user-1@gmail.com", null, LanguageEnum.ENGLISH, 1, 1)

        repository.save(user1).block()
    }

    void "GET should return the DTO of current user"() {
        when:
        def request = HttpRequest.GET("/currentUser").bearerAuth(tokenUser1)
        def response = client.toBlocking().exchange(request, Object)

        then:
        response.body() == [
            id         : "50b1cccd-2cbd-459b-9b63-c97145b97e94",
            email      : "user-1@gmail.com",
            language   : LanguageEnum.ENGLISH.toString(),
            dateCreated: 1,
            dateUpdated: 1,
        ]
    }

    void "GET should return 404 response if the user doesn't exist"() {
        when:
        def request = HttpRequest.GET("/currentUser").bearerAuth(tokenUser2)
        client.toBlocking().exchange(request, Object)

        then:
        HttpClientResponseException ex = thrown()
        ex.response.code() == HttpStatus.NOT_FOUND.code
    }

    @Unroll
    void "POST should create a new #type user from authorization"() {
        given: "user command"
        def command = [
            type                : type,
            primaryLanguage     : LanguageEnum.ENGLISH.toString(),
            translationLanguages: translationLanguages,
        ]

        when:
        def request = HttpRequest.POST("/currentUser", command).bearerAuth(tokenUser3)
        def response = client.toBlocking().exchange(request, Map)

        then:
        def body = response.body()
        body.email == "user-3@gmail.com"
        body.type == type
        body.primaryLanguage == LanguageEnum.ENGLISH.toString()
        body.translationLanguages == translationLanguages

        cleanup:
        repository.deleteAll()

        where:
        type                             | translationLanguages
        UserType.REGULAR_USER.toString() | null
        UserType.TRANSLATOR.toString()   | [LanguageEnum.ENGLISH.toString(), LanguageEnum.UKRAINIAN.toString()]
    }
}
