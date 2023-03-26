package com.rest_service

import com.rest_service.domain.User
import com.rest_service.enums.LanguageEnum
import com.rest_service.repository.UserRepository
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.annotation.Client
import io.micronaut.reactor.http.client.ReactorHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification
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

    def conditions = new PollingConditions(timeout: 5, initialDelay: 0.5, factor: 1.25)

    def setupSpec() {
        // email : user-1@gmail.com
        tokenUser1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJ1c2VyLTFAZ21haWwuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.cuAPn7Dl7cS1Onf24fVsDO5Q6IKXAx7zrPsklkajrqY"

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
            language   : LanguageEnum.ENGLISH,
            dateCreated: 1,
            dateUpdated: 1,
        ]
    }
}
